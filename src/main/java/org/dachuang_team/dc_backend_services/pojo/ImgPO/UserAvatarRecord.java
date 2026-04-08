package org.dachuang_team.dc_backend_services.pojo.ImgPO;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_avatar")
public class UserAvatarRecord {
    // 用户头像表，保存用户上传的头像信息
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;        // 关联用户ID
    private String avatarUrl;   // 头像URL
    private LocalDateTime uploadAt; // 上传时间
    private boolean isLinked = false;
    private boolean processed = false; // 是否已处理成正式头像（true）或临时原图（false）

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getUploadAt() {
        return uploadAt;
    }

    public void setUploadAt(LocalDateTime uploadAt) {
        this.uploadAt = uploadAt;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinked(boolean linked) {
        isLinked = linked;
    }
}
