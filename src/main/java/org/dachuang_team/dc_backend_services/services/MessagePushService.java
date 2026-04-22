package org.dachuang_team.dc_backend_services.services;

import lombok.RequiredArgsConstructor;
import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Conversation;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessagePushService {

    private final SimpMessagingTemplate messagingTemplate;

    // 推送消息给指定用户,用户、商家、客服端订阅：/user/queue/message
    // 用于一对一私聊
    public void pushChatMessage(Long userId, ConversationUserRole role, Object payload) {
        String principal = buildPrincipal(userId, role);
        messagingTemplate.convertAndSendToUser(principal, "/queue/message", payload);
    }

    // 广播待受理请求给所有在线客服
    // 客服端订阅：/topic/admin/pending-requests
    // ACTION: ADD/REMOVE
    public void broadcastPendingRequest(String action, Conversation conv) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", action);
        payload.put("conversationId", conv.getId());

        if ("ADD".equals(action)) {
            payload.put("initiatorRole", conv.getInitiatorRole().name());
            payload.put("initiatorId", conv.getInitiatorId());
            payload.put("conversationType", conv.getConversationType().name());
            payload.put("createdAt", conv.getCreatedAt());
        }

        messagingTemplate.convertAndSend("/topic/admin/pending-requests", payload);
    }

    // （系统消息）通知会话双方：会话已受理
    // 用户、商家、客服端订阅：/user/queue/notify
    public void pushAccepted(Long userId, ConversationUserRole role, Long conversationId, Long adminId) {
        Map<String, Object> payload = Map.of(
                "event", "ACCEPTED",
                "conversationId", conversationId,
                "adminId", adminId
        );
        String principal = buildPrincipal(userId, role);
        messagingTemplate.convertAndSendToUser(principal, "/queue/notify", payload);
    }

    // （系统消息）通知会话双方：会话已关闭
    // 用户、商家、客服端订阅：/user/queue/notify
    public void pushClosed(Long userId, ConversationUserRole role, Long conversationId, String reason) {
        Map<String, Object> payload = Map.of(
                "event", "CLOSED",
                "conversationId", conversationId,
                "reason", reason
        );
        String principal = buildPrincipal(userId, role);
        messagingTemplate.convertAndSendToUser(principal, "/queue/notify", payload);
    }

    private String buildPrincipal(Long userId, ConversationUserRole role) {
        return userId + "_" + role.name();
    }
}
