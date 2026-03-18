package org.dachuang_team.dc_backend_services.pojo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "points_record")
public class PointsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;
    @Column
    private Integer PointsChange; // 积分变动数量，正数表示增加，负数表示减少
    @Column
    private String reason; // 积分变动原因
    @Column
    private LocalDateTime changeTime; // 积分变动时间

    public PointsRecord() {
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

    public Integer getPointsChange() {
        return PointsChange;
    }

    public void setPointsChange(Integer pointsChange) {
        PointsChange = pointsChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }
}
