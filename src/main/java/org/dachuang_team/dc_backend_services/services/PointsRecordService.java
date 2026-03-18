package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.enumeration.PointsChangeReason;
import org.dachuang_team.dc_backend_services.pojo.PointsRecord;
import org.dachuang_team.dc_backend_services.repository.PointsRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointsRecordService implements IPointsRecordService {

    @Autowired
    private PointsRecordRepository pointsRecordRepository;

    @Override
    public boolean addPointsRecord(Long userId, Integer points, String reason) {

        if(!PointsChangeReason.isValidReason(reason)) {
            throw new IllegalArgumentException("无效的积分变动原因: " + reason);
        }

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPointsChange(points);
        record.setReason(reason);
        record.setChangeTime(LocalDateTime.now());

        pointsRecordRepository.save(record);
        return true;
    }

    @Override
    public List<PointsRecord> getPointsRecords(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return pointsRecordRepository.findByUserIdAndChangeTimeAfterOrderByChangeTimeDesc(userId, thirtyDaysAgo);
    }
}
