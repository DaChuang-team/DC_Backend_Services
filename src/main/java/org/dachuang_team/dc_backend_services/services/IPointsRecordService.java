package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserPointsRecord;

import java.util.List;

public interface IPointsRecordService {
    boolean addPointsRecord(Long userId, Integer points, String description);
    List<UserPointsRecord> getPointsRecords(Long userId);
}
