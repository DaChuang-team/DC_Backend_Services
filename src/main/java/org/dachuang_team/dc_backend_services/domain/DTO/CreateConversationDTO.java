package org.dachuang_team.dc_backend_services.domain.DTO;

import jakarta.validation.constraints.NotBlank;

public class CreateConversationDTO {
    @NotBlank(message = "会话类型不能为空")
    private String conversationType;
    private String targetRole;
    private Long targetId;
    private Long entryProductId;

    public Long getEntryProductId() {
        return entryProductId;
    }
    public void setEntryProductId(Long entryProductId) {
        this.entryProductId = entryProductId;
    }
    public Long getTargetId() {
        return targetId;
    }
    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }
    public String getTargetRole() {
        return targetRole;
    }
    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }
    public String getConversationType() {
        return conversationType;
    }
    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }
}
