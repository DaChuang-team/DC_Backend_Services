package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;

public interface IAccommodationService {
    AccommodationVO addAccommodation(AccommodationDTO accommodationDTO,Long merchantId);
}
