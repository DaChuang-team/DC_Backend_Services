package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.UserAvatarRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAvatarRecordRepository extends CrudRepository<UserAvatarRecord, Long> {
    UserAvatarRecord findByAvatarUrl(String avatarUrl);

    List<UserAvatarRecord> findByUserId(Long userId);
    List<UserAvatarRecord> findAllByUploadAtBeforeAndIsLinkedFalse(LocalDateTime time);

    void deleteByAvatarUrl(String url);
}
