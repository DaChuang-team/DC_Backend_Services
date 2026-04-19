package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.UserAvatar;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAvatarRecordRepository extends CrudRepository<UserAvatar, Long> {
    UserAvatar findByAvatarUrl(String avatarUrl);

    List<UserAvatar> findByUserId(Long userId);
    List<UserAvatar> findAllByUploadAtBeforeAndIsLinkedFalse(LocalDateTime cutoffTime);

    void deleteByAvatarUrl(String url);
}
