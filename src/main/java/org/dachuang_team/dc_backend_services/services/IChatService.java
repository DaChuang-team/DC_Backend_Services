package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.CreateConversationDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.SendMessageDTO;
import org.dachuang_team.dc_backend_services.domain.VO.ConversationVO;
import org.dachuang_team.dc_backend_services.domain.VO.MessageVO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IChatService {
    ConversationVO createConversation(CreateConversationDTO createConversationDTO, Long InitiatorId, String InitiatorRole);

    MessageVO sendMessage(SendMessageDTO dto, Long senderId, String senderRole);

    void callBackMessage(Long conversationId, Long messageId, Long senderId, String senderRole);

    ConversationVO handelServiceRequest(Long conversationId, Long adminId);

    ConversationVO closeConversation(Long conversationId, Long operatorId, String operatorRole, String reason);

    Page<ConversationVO> getPendingConversations(Long adminId, String conversationType, int page, int size);

    Page<ConversationVO> getConversations(Long userId, String userRole, String conversationType, int page, int size);

    // 分页查询某个会话内的历史消息数据
    Page<MessageVO> getMessages(Long conversationId, Long userId, String userRole, int page, int size);

    void setReadByMessageId(Long messageId, Long ConversationId, Long userId, String userRole);

    void markAsReadByConversationId(Long conversationId, Long userId, String userRole);

    Map<String, Object> getUserNameAndAvatar(Long userId);
}
