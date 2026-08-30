package org.dachuang_team.dc_backend_services.domain.PO.ImgPO;

import jakarta.persistence.*;
import org.dachuang_team.dc_backend_services.domain.PO.Conversations.Conversation;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_img")
public class ConversationImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imgUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "upload_user_role", nullable = false)
    private ConversationUserRole uploadUserRole;
    @Column(name = "upload_user_id", nullable = false)
    private Long uploadUserId;
    @Column(name = "upload_at")
    private LocalDateTime uploadAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public ConversationUserRole getUploadUserRole() {
        return uploadUserRole;
    }

    public void setUploadUserRole(ConversationUserRole uploadUserRole) {
        this.uploadUserRole = uploadUserRole;
    }

    public Long getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public LocalDateTime getUploadAt() {
        return uploadAt;
    }

    public void setUploadAt(LocalDateTime uploadAt) {
        this.uploadAt = uploadAt;
    }
}
