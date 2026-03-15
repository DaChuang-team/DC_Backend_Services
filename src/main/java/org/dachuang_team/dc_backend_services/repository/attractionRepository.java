package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface attractionRepository extends JpaRepository<Attraction, Long> {
    // 根据标签查找，用于区分 Hotel 和 其他景点
    java.util.List<Attraction> findByAttractionTag(String attractionTag);
    // 查找不等于某个标签的，用于获取所有非酒店的景点
    java.util.List<Attraction> findByAttractionTagNot(String attractionTag);
}
