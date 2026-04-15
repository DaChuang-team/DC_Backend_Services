package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;
import org.dachuang_team.dc_backend_services.services.AccommodationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accommodations")
public class AccommodationController {

    @Autowired
    private AccommodationService accommodationService;

    // 酒店发布
    @PutMapping("/addAccommodation")
    public ResponseEntity<Result<AccommodationVO>> addAccommodation(
            @RequestBody AccommodationDTO accommodationDTO) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccommodationVO accommodationVO = accommodationService.addAccommodation(accommodationDTO, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "酒店发布成功", accommodationVO));
    }
}
