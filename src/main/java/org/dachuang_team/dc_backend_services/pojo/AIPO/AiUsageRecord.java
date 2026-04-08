package org.dachuang_team.dc_backend_services.pojo.AIPO;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage_record")
public class AiUsageRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long tokenCount;      // 记录本次交互使用的Token数量
    private String type;             // 记录交互类型（如文本、图片等）
    private LocalDateTime requestTime; // 记录交互的时间

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

    public Long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
}
