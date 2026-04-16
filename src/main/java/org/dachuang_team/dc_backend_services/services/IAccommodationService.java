package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;

import java.util.Map;

public interface IAccommodationService {
    AccommodationVO addAccommodation(AccommodationDTO accommodationDTO,Long merchantId);

    AccommodationVO updateAccommodation(Long accommodationId, AccommodationDTO accommodationDTO, Long merchantId);

    Void deleteAccommodation(Long accommodationId, Long merchantId);

    AccommodationVO getAccommodation(Long accommodationId);

    Map<String, Object> searchAccommodationsByUser(String keyword, Double minPrice, Double latitude, Double longitude, String type,Integer page, Integer size);

    Map<String, Object> searchAccommodationsByMerchant(Long merchantId, String keyword, Integer page, Integer size);
}
