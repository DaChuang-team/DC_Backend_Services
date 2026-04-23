package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.CreateConversationDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.SendMessageDTO;
import org.dachuang_team.dc_backend_services.domain.VO.ConversationVO;
import org.dachuang_team.dc_backend_services.domain.VO.MessageVO;
import org.dachuang_team.dc_backend_services.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/createConversation")
    public Result<ConversationVO> createConversation(@Validated @RequestBody CreateConversationDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Long initiatorId = (Long) authentication.getPrincipal();

            String initiatorRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            ConversationVO conversationVO = chatService.createConversation(dto, initiatorId, initiatorRole);

            return Result.success(200, "会话创建成功", conversationVO);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @PostMapping("/service/handleServiceRequest")
    public Result<ConversationVO> handleServiceRequest(@RequestParam Long conversationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Long initiatorId = (Long) authentication.getPrincipal();

            ConversationVO conversationVO = chatService.handelServiceRequest(conversationId, initiatorId);

            return Result.success(200, "会话受理成功", conversationVO);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @PostMapping("/closeConversation")
    public Result<ConversationVO> closeConversation(@RequestParam Long conversationId,
                                                    @RequestParam(required = false) String Reason) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Long initiatorId = (Long) authentication.getPrincipal();

            String initiatorRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            ConversationVO conversationVO = chatService.closeConversation(conversationId, initiatorId, initiatorRole, Reason);
            return Result.success(200, "会话关闭成功", conversationVO);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @GetMapping("/service/pendingList")
    public Result<Map<String, Object>> getPendingList(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String conversationType) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Long initiatorId = (Long) authentication.getPrincipal();

            Page<ConversationVO> conversationPage = chatService.getPendingConversations(initiatorId, conversationType, page - 1, size);

            return getConversationsMapResponseEntity(conversationPage);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }


    @PostMapping("/sendMessage")
    public Result<MessageVO> sendMessage(@Validated @RequestBody SendMessageDTO dto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            Long senderId = (Long) authentication.getPrincipal();

            String senderRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            MessageVO messageVO = chatService.sendMessage(dto, senderId, senderRole);

            return Result.success(200, "消息发送成功", messageVO);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        }catch (IllegalStateException e){
            return Result.error(409, "会话状态异常: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    //分页查询当前用户的会话列表
    @GetMapping("/conversations")
    public Result<Map<String, Object>> getConversations(
            @RequestParam String conversationType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getPrincipal();
            String userRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            Page<ConversationVO> conversationPage = chatService.getConversations(userId, userRole, conversationType, page - 1, size);

            return getConversationsMapResponseEntity(conversationPage);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }


    // 分页查询指定会话内的历史消息
    @GetMapping("/messages")
    public Result<Map<String, Object>> getMessages(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getPrincipal();
            String userRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            Page<MessageVO> messagePage = chatService.getMessages(conversationId, userId, userRole, page - 1, size);
            return getMessagesMapResponseEntity(messagePage);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @PostMapping("/markAsReadByMessageId")
    public Result<String> setReadByMessageId(@RequestParam Long messageId,
                                             @RequestParam Long conversationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getPrincipal();
            String userRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            chatService.setReadByMessageId(messageId, conversationId, userId, userRole);
            return Result.success(200, "消息" + messageId + "已标记为已读", null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    @PostMapping("/markAsReadByConversationId")
    public Result<String> markOtherMessagesAsRead(@RequestParam Long conversationId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getPrincipal();
            String userRole = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);
            chatService.markAsReadByConversationId(conversationId, userId, userRole);
            return Result.success(200, "会话" + conversationId + "中其他消息已标记为已读", null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

    // 获取用户的用户名和头像URL用于聊天界面显示
    // 注意：只能查询用户的头像和昵称
    @GetMapping("/userInfo")
    public Result<Map<String, Object>> getUserInfo(@RequestParam Long userId) {
        try {
            Map<String, Object> userInfo = chatService.getUserNameAndAvatar(userId);
            return Result.success(200, "查询成功", userInfo);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }


    private Result<Map<String, Object>> getConversationsMapResponseEntity(Page<ConversationVO> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("conversations", page.getContent());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber() + 1);

        return Result.success(200, "会话列表查询成功", response);
    }

    private Result<Map<String, Object>> getMessagesMapResponseEntity(Page<MessageVO> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("messages", page.getContent());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("currentPage", page.getNumber() + 1);

        return Result.success(200, "历史消息查询成功", response);
    }
}
