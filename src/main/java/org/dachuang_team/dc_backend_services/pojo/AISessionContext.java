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

    private String lastResponseId; // 存储最近一次Responses API的ID
    private String modelEndpoint;  // 记录使用的终端节点，追问时不支持切换其他版本模型回答
    private LocalDateTime lastResponseTime; // 记录最近一次Responses API的时间戳

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
}