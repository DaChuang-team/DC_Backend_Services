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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
public class ChatService implements IChatService {
    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional(rollbackOn = Exception.class)
    public ConversationVO createConversation(CreateConversationDTO dto, Long initiatorId, String role) {
        ConversationType typeEnum;
        try {
            typeEnum = ConversationType.valueOf(dto.getConversationType());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("无效的会话类型: " + dto.getConversationType());
        }

        if (typeEnum == ConversationType.USER_MERCHANT) {
            // USER_MERCHANT 规定只能由 USER 主动发起，商家只能复用已有的会话
            if (!"USER".equals(role)) {
                throw new IllegalStateException("只有普通用户可以主动发起与商家的会话");
            }

            if(!merchantRepository.existsById(dto.getTargetId())) {
                throw new IllegalArgumentException("目标商家不存在！");
            }

            ConversationUserRole initiatorRoleEnum = ConversationUserRole.valueOf(role);
            ConversationUserRole targetRoleEnum = ConversationUserRole.MERCHANT;

            // 查找是否已存在该 用户 和 商家 之间的会话
            Conversation conversation = conversationRepository.findByInitiatorIdAndInitiatorRoleAndTargetIdAndTargetRole(
                    initiatorId, initiatorRoleEnum, dto.getTargetId(), targetRoleEnum
            );

            if (conversation != null) {
                // 存在旧会话，检查是否需要更新 entryProductId
                if (dto.getEntryProductId() != null) {
                    // 验证这个 productId 是否有效
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

            // 组装并返回 VO
            return buildConversationVO(conversation, initiatorId, initiatorRoleEnum);
        }

        //TODO: 如果后续支持其他会话类型，在这里继续添加分支处理
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
        message.setSenderId(senderId);     // 为了安全，强制采用鉴权得到的id
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
                    pushNewMessage(conversation, senderId, senderRole, vo);
                } catch (Exception e) {
                    System.err.println("消息推送失败: " + e.getMessage());
                }
            }
        });

        return vo;
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

        // 权限校验：确保查询人是该会话发起方或接受方
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


    // 辅助方法：将实体转成 VO 返回
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

    private void pushNewMessage(Conversation conv, Long senderId, String senderRole, MessageVO vo) {
        Long receiverId;
        String receiverRole;

        if (conv.getInitiatorId().equals(senderId) && conv.getInitiatorRole().name().equals(senderRole)) {
            receiverId   = conv.getTargetId();
            // 注意：这里需要配合枚举获取名字
            receiverRole = conv.getTargetRole().name();
        } else {
            receiverId   = conv.getInitiatorId();
            receiverRole = conv.getInitiatorRole().name();
        }

        // principalName 格式和 Interceptor 一致 userId_userRole
        String principalName = receiverId + "_" + receiverRole;

        // convertAndSendToUser 最终实际通道会被映射到： /user/{principalName}/queue/message
        // 客户端主动订阅 /user/queue/message 即可
        messagingTemplate.convertAndSendToUser(
                principalName,
                "/queue/message",
                vo
        );
    }

}
