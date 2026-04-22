package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateConversationDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.SendMessageDTO;
import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Conversation;
import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Message;
import org.dachuang_team.dc_backend_services.domain.VO.ConversationVO;
import org.dachuang_team.dc_backend_services.domain.VO.MessageVO;
import org.dachuang_team.dc_backend_services.enumeration.ConversationStatus;
import org.dachuang_team.dc_backend_services.enumeration.ConversationType;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.dachuang_team.dc_backend_services.enumeration.MsgType;
import org.dachuang_team.dc_backend_services.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService implements IChatService {
    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;


    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MessagePushService messagePushService;

    @Transactional(rollbackOn = Exception.class)
    public ConversationVO createConversation(CreateConversationDTO dto, Long initiatorId, String role) {
        ConversationType typeEnum;
        ConversationUserRole initiatorRoleEnum;
        try {
            typeEnum = ConversationType.valueOf(dto.getConversationType());
            initiatorRoleEnum = ConversationUserRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("无效的会话类型: " + dto.getConversationType());
        }

        if (typeEnum == ConversationType.USER_MERCHANT) {
            // USER_MERCHANT只能由USER主动发起，商家只能复用已有的会话
            if (!"USER".equals(role)) {
                throw new IllegalStateException("只有普通用户可以主动发起与商家的会话");
            }

            if(!merchantRepository.existsById(dto.getTargetId())) {
                throw new IllegalArgumentException("目标商家不存在！");
            }

            initiatorRoleEnum = ConversationUserRole.valueOf(role);
            ConversationUserRole targetRoleEnum = ConversationUserRole.MERCHANT;

            // 查找是否已存在该 用户 和 商家 之间的会话
            Conversation conversation = conversationRepository.findByInitiatorIdAndInitiatorRoleAndTargetIdAndTargetRole(
                    initiatorId, initiatorRoleEnum, dto.getTargetId(), targetRoleEnum
            );

            if (conversation != null) {
                // 存在旧会话，检查是否需要更新entryProductId
                if (dto.getEntryProductId() != null) {
                    // 验证productId是否有效
                    if(productRepository.existsById(dto.getEntryProductId())) {
                        conversation.setEntryProductId(dto.getEntryProductId());
                        conversation.setUpdatedAt(LocalDateTime.now());
                        conversation = conversationRepository.save(conversation);
                    }
                }
            } else {
                // 不存在，新建会话
                conversation = new Conversation();
                conversation.setConversationType(ConversationType.USER_MERCHANT);
                conversation.setInitiatorId(initiatorId);
                conversation.setInitiatorRole(initiatorRoleEnum);
                conversation.setTargetId(dto.getTargetId());
                conversation.setTargetRole(targetRoleEnum);
                if(dto.getEntryProductId() != null) {
                    if(productRepository.existsById(dto.getEntryProductId())) {
                        conversation.setEntryProductId(dto.getEntryProductId());
                    }
                } else {
                    conversation.setEntryProductId(null);
                }

                // 默认状态
                conversation.setStatus(ConversationStatus.ACTIVE);
                LocalDateTime now = LocalDateTime.now();
                conversation.setCreatedAt(now);
                conversation.setUpdatedAt(now);

                conversation = conversationRepository.save(conversation);
            }

            return buildConversationVO(conversation, initiatorId, initiatorRoleEnum);
        }

        if (typeEnum == ConversationType.USER_CUSTOMER_SERVICE
                || typeEnum == ConversationType.MERCHANT_CUSTOMER_SERVICE) {
            Conversation conversation = handleCustomerServiceConversation(
                    initiatorId, initiatorRoleEnum, typeEnum, dto.getEntryProductId()
            );
            return buildConversationVO(conversation, initiatorId, initiatorRoleEnum);
        }

        throw new UnsupportedOperationException("暂不支持该会话类型: " + typeEnum.name());
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public MessageVO sendMessage(SendMessageDTO dto, Long senderId, String senderRole) {
        // 检查会话是否存在
        Conversation conversation = conversationRepository.findById(dto.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("当前会话不存在！"));

        // 将发送者的role字符串转换为枚举
        ConversationUserRole roleEnum;
        try {
            roleEnum = ConversationUserRole.valueOf(senderRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("非法的发送者角色: " + senderRole);
        }

        // 验证当前的(senderId, senderRole)是否就是这则会话的发起方或承接方
        boolean isInitiator = senderId.equals(conversation.getInitiatorId()) && roleEnum == conversation.getInitiatorRole();
        boolean isTarget = senderId.equals(conversation.getTargetId()) && roleEnum == conversation.getTargetRole();

        if (!isInitiator && !isTarget) {
            throw new IllegalArgumentException("无权在该会话中发送消息：当前用户不在该会话中");
        }

        // 客服会话中，客服在会话已关闭时禁止发送
        boolean isCustomerServiceConversation =
                conversation.getConversationType() == ConversationType.USER_CUSTOMER_SERVICE
                        || conversation.getConversationType() == ConversationType.MERCHANT_CUSTOMER_SERVICE;

        if (isCustomerServiceConversation
                && roleEnum == ConversationUserRole.ADMIN
                && conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new IllegalStateException("会话已被关闭，客服不可继续发送消息");
        }

        // 解析消息类型并落库
        MsgType msgTypeEnum;
        try {
            msgTypeEnum = MsgType.valueOf(dto.getMsgType());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("不支持的消息类型: " + dto.getMsgType());
        }

        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setMsgType(msgTypeEnum);
        message.setSenderId(senderId);     // 强制采用鉴权得到的id
        message.setSenderRole(roleEnum);   // 强制采用鉴权得到的role
        message.setContent(dto.getContent());
        message.setRead(false);            // 默认未读

        LocalDateTime now = LocalDateTime.now();
        message.setCreatedAt(now);

        message = messageRepository.save(message);

        // 更新会话的最新活跃时间
        conversation.setUpdatedAt(now);
        conversationRepository.save(conversation);

        MessageVO vo = buildMessageVO(message);

        //确保事务提交后再推送
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    Long receiverId;
                    ConversationUserRole receiverRole;

                    if (isInitiator) {
                        receiverId = conversation.getTargetId();
                        receiverRole = conversation.getTargetRole();
                    } else {
                        receiverId = conversation.getInitiatorId();
                        receiverRole = conversation.getInitiatorRole();
                    }

                    messagePushService.pushChatMessage(receiverId, receiverRole, vo);
                } catch (Exception e) {
                    System.err.println("消息推送失败: " + e.getMessage());
                }
            }
        });

        return vo;
    }

    @Override
    public Page<ConversationVO> getPendingConversations(Long adminId, String conversationType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<ConversationType> typeList;

        if (conversationType != null && !conversationType.isBlank()) {
            ConversationType typeEnum;
            try {
                typeEnum = ConversationType.valueOf(conversationType);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的会话类型: " + conversationType);
            }
            typeList = List.of(typeEnum);
        } else {
            typeList = List.of(ConversationType.USER_CUSTOMER_SERVICE, ConversationType.MERCHANT_CUSTOMER_SERVICE);
        }

        Page<Conversation> conversationPage = conversationRepository.findPendingCustomerServiceConversations(
                ConversationStatus.PENDING,
                typeList,
                pageable
        );

        return conversationPage.map(conv -> buildConversationVO(conv, adminId, ConversationUserRole.ADMIN));
    }


    @Override
    public Page<ConversationVO> getConversations(Long userId, String userRole, String conversationType, int page, int size) {
        ConversationUserRole roleEnum;
        ConversationType typeEnum;
        try {
            roleEnum = ConversationUserRole.valueOf(userRole);
            typeEnum = ConversationType.valueOf(conversationType);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("无效的参数类型(枚举类不存在)！");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Conversation> conversationPage = conversationRepository.findUserConversationsWithType(
                userId, roleEnum, typeEnum, pageable
        );

        return conversationPage.map(conv -> buildConversationVO(conv, userId, roleEnum));
    }


    @Override
    public Page<MessageVO> getMessages(Long conversationId, Long userId, String userRole, int page, int size) {
        // 验证会话是否存在
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("当前会话不存在！"));

        // 将发送者的role字符串转换为枚举
        ConversationUserRole roleEnum;
        try {
            roleEnum = ConversationUserRole.valueOf(userRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("非法的用户角色!");
        }

        // 确保查询人是该会话发起方或接受方
        boolean isInitiator = userId.equals(conversation.getInitiatorId()) && roleEnum == conversation.getInitiatorRole();
        boolean isTarget = userId.equals(conversation.getTargetId()) && roleEnum == conversation.getTargetRole();

        if (!isInitiator && !isTarget) {
            throw new IllegalArgumentException("无权查看该会话的历史消息：当前用户不在该会话中");
        }

        // 构建分页请求
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);

        // 只将"对方发来"的未读消息标记为已读
        messageRepository.markOtherMessagesAsRead(conversationId, userId, roleEnum);

        return messagePage.map(this::buildMessageVO);
    }

    // 将指定消息标记为已读（实时聊天场景下，用户查看了某条消息后，前端会调用这个接口来标记该消息为已读）
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void setReadByMessageId(Long messageId, Long conversationId, Long userId, String userRole) {
        // 验证会话是否存在
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("当前会话不存在！"));
        ConversationUserRole roleEnum;

        try {
            roleEnum = ConversationUserRole.valueOf(userRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("非法的用户角色!");
        }

        boolean isInitiator = userId.equals(conversation.getInitiatorId()) && roleEnum == conversation.getInitiatorRole();
        boolean isTarget = userId.equals(conversation.getTargetId()) && roleEnum == conversation.getTargetRole();

        if (!isInitiator && !isTarget) {
            throw new IllegalArgumentException("无权操作该会话的消息：当前用户不在该会话中");
        }

        messageRepository.setIsReadTrueById(messageId);
    }

    // 将当前会话中，别人发给当前用户的未读消息标记为已读
    // 查询时默认会重新统计未读数，该方法用于手动点击"标记已读"按钮的场景，或者在某些特殊场景下需要批量标记已读时调用
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void markAsReadByConversationId(Long conversationId, Long userId, String userRole) {
        // 验证会话是否存在
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("当前会话不存在！"));
        // 将发送者的role字符串转换为枚举
        ConversationUserRole roleEnum;
        try {
            roleEnum = ConversationUserRole.valueOf(userRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("非法的用户角色!");
        }

        // 权限校验：确保查询人是该会话发起方或接受方
        boolean isInitiator = userId.equals(conversation.getInitiatorId()) && roleEnum == conversation.getInitiatorRole();
        boolean isTarget = userId.equals(conversation.getTargetId()) && roleEnum == conversation.getTargetRole();

        if (!isInitiator && !isTarget) {
            throw new IllegalArgumentException("无权操作该会话的消息：当前用户不在该会话中");
        }

        messageRepository.markOtherMessagesAsRead(conversationId, userId, roleEnum);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ConversationVO handelServiceRequest(Long conversationId, Long adminId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("当前会话不存在！"));

        // 只允许处理客服相关会话
        if (conversation.getConversationType() != ConversationType.USER_CUSTOMER_SERVICE
                && conversation.getConversationType() != ConversationType.MERCHANT_CUSTOMER_SERVICE) {
            throw new IllegalArgumentException("当前会话不是客服会话，不能受理");
        }

        // 只有待处理中的请求可以被受理
        if (conversation.getStatus() != ConversationStatus.PENDING) {
            throw new IllegalStateException("只有待处理状态的会话才能受理");
        }

        LocalDateTime now = LocalDateTime.now();

        // 受理后绑定当前管理员
        conversation.setTargetId(adminId);
        conversation.setTargetRole(ConversationUserRole.ADMIN);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setClosedAt(null);
        conversation.setUpdatedAt(now);

        Conversation saved = conversationRepository.save(conversation);

        // 事务提交后再推送
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // 通知发起者会话已受理
                    messagePushService.pushAccepted(
                            saved.getInitiatorId(),
                            saved.getInitiatorRole(),
                            saved.getId(),
                            adminId
                    );

                    // 通知当前管理员会话已受理
                    messagePushService.pushAccepted(
                            adminId,
                            ConversationUserRole.ADMIN,
                            saved.getId(),
                            adminId
                    );

                    // 从客服待受理列表移除
                    messagePushService.broadcastPendingRequest("REMOVE", saved);
                } catch (Exception e) {
                    System.err.println("受理通知推送失败: " + e.getMessage());
                }
            }
        });

        return buildConversationVO(saved, adminId, ConversationUserRole.ADMIN);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ConversationVO closeConversation(Long conversationId, Long operatorId, String operatorRole, String reason) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("当前会话不存在！"));

        ConversationUserRole roleEnum;
        try {
            roleEnum = ConversationUserRole.valueOf(operatorRole);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("非法的操作者角色: " + operatorRole);
        }

        // 必须属于会话双方之一才能关闭
        boolean isInitiator = operatorId.equals(conversation.getInitiatorId()) && roleEnum == conversation.getInitiatorRole();
        boolean isTarget = operatorId.equals(conversation.getTargetId()) && roleEnum == conversation.getTargetRole();

        if (!isInitiator && !isTarget) {
            throw new IllegalArgumentException("无权关闭该会话：当前用户不在该会话中");
        }

        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new IllegalStateException("会话已是关闭状态");
        }

        if(conversation.getConversationType() != ConversationType.USER_CUSTOMER_SERVICE
                && conversation.getConversationType() != ConversationType.MERCHANT_CUSTOMER_SERVICE) {
            throw new IllegalArgumentException("当前会话不属于客服会话，无法关闭");
        }

        ConversationStatus preStatus = conversation.getStatus();

        LocalDateTime now = LocalDateTime.now();
        conversation.setStatus(ConversationStatus.CLOSED);
        conversation.setClosedAt(now);
        conversation.setUpdatedAt(now);

        Conversation saved = conversationRepository.save(conversation);

        String closeReason = (reason == null || reason.isBlank()) ? "会话已关闭" : reason;

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // 若是从待受理状态被关闭，同步移出客服待受理列表
                    if (preStatus == ConversationStatus.PENDING){
                        messagePushService.broadcastPendingRequest("REMOVE", saved);
                    }

                    // 通知发起方
                    messagePushService.pushClosed(
                            saved.getInitiatorId(),
                            saved.getInitiatorRole(),
                            saved.getId(),
                            closeReason
                    );

                    // 通知承接方
                    if (saved.getTargetId() != null && saved.getTargetRole() != null) {
                        messagePushService.pushClosed(
                                saved.getTargetId(),
                                saved.getTargetRole(),
                                saved.getId(),
                                closeReason
                        );
                    }
                } catch (Exception e) {
                    System.err.println("关闭通知推送失败: " + e.getMessage());
                }
            }
        });

        return buildConversationVO(saved, operatorId, roleEnum);
    }

    private Conversation handleCustomerServiceConversation(Long initiatorId,
                                                           ConversationUserRole initiatorRole,
                                                           ConversationType conversationType,
                                                           Long entryProductId) {
        // 类型与发起角色强校验
        if (conversationType == ConversationType.USER_CUSTOMER_SERVICE
                && initiatorRole != ConversationUserRole.USER) {
            throw new IllegalStateException("用户客服请求只能由用户发起");
        }
        if (conversationType == ConversationType.MERCHANT_CUSTOMER_SERVICE
                && initiatorRole != ConversationUserRole.MERCHANT) {
            throw new IllegalStateException("商家客服请求只能由商家发起");
        }

        LocalDateTime now = LocalDateTime.now();

        // 查该发起者该类型下的历史会话（按最近活跃时间倒序）
        var history = conversationRepository
                .findByInitiatorIdAndInitiatorRoleAndConversationTypeOrderByUpdatedAtDesc(
                        initiatorId, initiatorRole, conversationType
                );

        // 优先返回ACTIVE/PENDING
        for (Conversation c : history) {
            if (c.getStatus() == ConversationStatus.ACTIVE || c.getStatus() == ConversationStatus.PENDING) {
                return c;
            }
        }

        // 存在CLOSED，重设为PENDING并重新发起受理请求
        for (Conversation c : history) {
            if (c.getStatus() == ConversationStatus.CLOSED) {
                c.setStatus(ConversationStatus.PENDING);
                c.setClosedAt(null);
                c.setUpdatedAt(now);

                // 重新排队等待客服受理：目标客服置空，角色固定ADMIN
                c.setTargetRole(ConversationUserRole.ADMIN);
                c.setTargetId(null);

                Conversation reopened = conversationRepository.save(c);
                registerPendingRequestAfterCommit(reopened);
                return reopened;
            }
        }

        // 无历史会话，新建PENDING会话并发起受理请求
        Conversation created = new Conversation();
        created.setConversationType(conversationType);
        created.setInitiatorId(initiatorId);
        created.setInitiatorRole(initiatorRole);

        // 未受理前尚未绑定具体客服
        created.setTargetRole(ConversationUserRole.ADMIN);
        created.setTargetId(null);

        created.setEntryProductId(entryProductId);
        created.setStatus(ConversationStatus.PENDING);
        created.setCreatedAt(now);
        created.setUpdatedAt(now);
        created.setClosedAt(null);

        created = conversationRepository.save(created);
        registerPendingRequestAfterCommit(created);
        return created;
    }

    private void registerPendingRequestAfterCommit(Conversation conversation) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    messagePushService.broadcastPendingRequest("ADD", conversation);
                } catch (Exception e) {
                    System.err.println("客服待受理请求广播失败: " + e.getMessage());
                }
            }
        });
    }



    private MessageVO buildMessageVO(Message message) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setMsgType(message.getMsgType().name());
        vo.setSenderRole(message.getSenderRole().name());
        vo.setSenderId(message.getSenderId());
        vo.setContent(message.getContent());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }

    private ConversationVO buildConversationVO(Conversation conversation, Long currentUserId, ConversationUserRole currentUserRole) {
        ConversationVO vo = new ConversationVO();
        vo.setConversationId(conversation.getId());
        vo.setInitiatorId(conversation.getInitiatorId());
        vo.setInitiatorRole(conversation.getInitiatorRole().name());
        vo.setTargetId(conversation.getTargetId());
        vo.setTargetRole(conversation.getTargetRole().name());

        // 使用加入视角的未读数统计
        vo.setUnreadCount(messageRepository.countUnreadMessagesForCurrentUser(
                conversation.getId(), currentUserId, currentUserRole));

        // 处理刚创建的会话或被清空消息的会话
        Message lastMessage = messageRepository.findTop1ByConversationIdOrderByCreatedAtDesc(conversation.getId());
        vo.setLastMessage(lastMessage != null ? lastMessage.getContent() : "");

        vo.setLastMessageTime(conversation.getUpdatedAt());

        return vo;
    }

}
