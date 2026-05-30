package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.ExternalLinkDTO;
import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.Accommodation;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationExternalLinksVO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;
import org.dachuang_team.dc_backend_services.domain.VO.ExternalLinkVO;

import java.util.List;
import java.util.Map;

public interface IAccommodationService {
    AccommodationVO addAccommodation(AccommodationDTO accommodationDTO,Long merchantId);

    AccommodationVO updateAccommodation(Long accommodationId, AccommodationDTO accommodationDTO, Long merchantId);

    Void deleteAccommodation(Long accommodationId, Long merchantId);

    AccommodationVO getAccommodation(Long accommodationId);

    Map<String, Object> searchAccommodationsByUser(String keyword, Double minPrice, Double latitude, Double longitude, String type,Integer page, Integer size);

    Map<String, Object> searchAccommodationsByMerchant(Long merchantId, String keyword, Integer page, Integer size);

    ExternalLinkVO addExternalLink(ExternalLinkDTO externalLinkDTO, Long accommodationId, Long merchantId);

    ExternalLinkVO updateExternalLink(Long externalLinkId, ExternalLinkDTO dto, Long merchantId);

    Void deleteExternalLink(Long externalLinkId, Long merchantId);

    List<ExternalLinkVO> showExternalLinksByMerchant(Long accommodationId, Long merchantId);

    List<ExternalLinkVO> showExternalLinksByUser(Long accommodationId);

    String topALink(Long externalLinkId, Long merchantId);

    String unTopALink(Long externalLinkId, Long merchantId);

    AccommodationVO approveAccommodation(Long accommodationId, boolean approved);

    List<Accommodation> getAllAccommodations();

    ExternalLinkVO approveExternalLink(Long externalLinkId, boolean approved);

    List<AccommodationExternalLinksVO> getAllAccommodationsWithExternalLinks();

}
