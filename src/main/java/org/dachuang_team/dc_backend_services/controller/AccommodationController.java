package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;
import org.dachuang_team.dc_backend_services.services.AccommodationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accommodations")
public class AccommodationController {

    @Autowired
    private AccommodationService accommodationService;

    // 酒店发布
    @PutMapping("/merchant/addAccommodation")
    public ResponseEntity<Result<AccommodationVO>> addAccommodation(
            @RequestBody AccommodationDTO accommodationDTO) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccommodationVO accommodationVO = accommodationService.addAccommodation(accommodationDTO, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "酒店发布成功", accommodationVO));
    }

    @PutMapping("/merchant/updateAccommodation")
    public ResponseEntity<Result<AccommodationVO>> updateAccommodation(
            @RequestParam Long accommodationId,
            @RequestBody AccommodationDTO accommodationDTO) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccommodationVO accommodationVO = accommodationService.updateAccommodation(accommodationId, accommodationDTO, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "酒店更新成功", accommodationVO));
    }

    @DeleteMapping("/merchant/deleteAccommodation")
    public ResponseEntity<Result<String>> deleteAccommodation(@RequestParam Long accommodationId) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        accommodationService.deleteAccommodation(accommodationId, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "酒店删除成功", null));
    }

    // 商家搜索自己的酒店
    @GetMapping("/merchant/search")
    public ResponseEntity<Result<Map<String, Object>>> searchMerchantAccommodations(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> data = accommodationService.searchAccommodationsByMerchant(
                currentMerchantId, keyword, page, size);

        return ResponseEntity.ok(Result.success(200, "商家酒店搜索成功", data));
    }


    // 根据酒店id获取酒店详情
    @GetMapping("/get")
    public ResponseEntity<Result<AccommodationVO>> getAccommodation(@RequestParam Long accommodationId) {
        AccommodationVO accommodationVO = accommodationService.getAccommodation(accommodationId);
        return ResponseEntity.ok(Result.success(200, "酒店详情获取成功", accommodationVO));
    }

    // 用户端搜索酒店
    @GetMapping("/user/search")
    public ResponseEntity<Result<Map<String, Object>>> searchAccommodations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Map<String, Object> data = accommodationService.searchAccommodationsByUser(
                keyword, minPrice, latitude, longitude, type, page, size);
        return ResponseEntity.ok(Result.success(200, "酒店搜索成功", data));
    }
}
