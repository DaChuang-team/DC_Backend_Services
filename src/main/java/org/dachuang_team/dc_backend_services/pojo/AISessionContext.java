package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_session_context")
public class AISessionContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long userId;        // 关联系统用户 ID

    private String sessionId; // 存储当前会话的唯一标识符

    private String lastResponseId; // 存储最近一次Responses API的ID
    private String modelEndpoint;  // 记录使用的终端节点，追问时不支持切换其他版本模型回答
    private LocalDateTime lastResponseTime; // 记录最近一次Responses API的时间戳
    private LocalDateTime expireTime; // 上次交互时间超过3天则过期，清除上下文

    public String getLastResponseId() {
        return lastResponseId;
    }

    public void setLastResponseId(String lastResponseId) {
        this.lastResponseId = lastResponseId;
    }

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

    public String getModelEndpoint() {
        return modelEndpoint;
    }

    public void setModelEndpoint(String modelEndpoint) {
        this.modelEndpoint = modelEndpoint;
    }

    public LocalDateTime getLastResponseTime() {
        return lastResponseTime;
    }

    public void setLastResponseTime(LocalDateTime lastResponseTime) {
        this.lastResponseTime = lastResponseTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}