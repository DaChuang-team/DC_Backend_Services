package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.Accommodation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    // 不传经纬度：按发布时间倒序
    @Query("""
            SELECT a.accommodationId, a.accommodationName, a.tbImageUrl, a.priceFrom,
                   a.amenities, a.policyNote
            FROM Accommodation a
            WHERE a.approved = true
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(a.accommodationName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR a.priceFrom >= :minPrice)
              AND (:type IS NULL OR a.type = :type)
            ORDER BY a.publishedAt DESC
            """)
    Page<Object[]> searchSummaryByPublishTime(
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("type") String type,
            Pageable pageable
    );

    // 传经纬度：按距离升序
    @Query(value = """
            SELECT a.accommodation_id, a.accommodation_name, a.tb_image_url, a.price_from,
                   ROUND(ST_Distance_Sphere(POINT(a.longitude, a.latitude), POINT(:longitude, :latitude)) / 1000, 2) AS distance_km,
                   a.amenities, a.policy_note
            FROM accommodation a
            WHERE a.approved = 1
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(a.accommodation_name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR a.price_from >= :minPrice)
              AND (:type IS NULL OR a.type = :type)
              AND a.latitude IS NOT NULL
              AND a.longitude IS NOT NULL
            ORDER BY ST_Distance_Sphere(POINT(a.longitude, a.latitude), POINT(:longitude, :latitude)) ASC, a.published_at DESC
            """,
            countQuery = """
            SELECT COUNT(1)
            FROM accommodation a
            WHERE a.approved = 1
              AND (:keyword IS NULL OR :keyword = '' OR LOWER(a.accommodation_name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:minPrice IS NULL OR a.price_from >= :minPrice)
              AND a.latitude IS NOT NULL
              AND a.longitude IS NOT NULL
            """,
            nativeQuery = true)
    Page<Object[]> searchSummaryByDistance(
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("type") String type,
            Pageable pageable
    );

    Page<Accommodation> findBySellerIdOrderByPublishedAtDesc(Long sellerId, Pageable pageable);

    Page<Accommodation> findBySellerIdAndAccommodationNameContainingIgnoreCaseOrderByPublishedAtDesc(
            Long sellerId, String keyword, Pageable pageable);

}
