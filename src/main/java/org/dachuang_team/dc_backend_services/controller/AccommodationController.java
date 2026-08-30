package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.ExternalLinkDTO;
import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.Accommodation;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationExternalLinksVO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;
import org.dachuang_team.dc_backend_services.domain.VO.ExternalLinkVO;
import org.dachuang_team.dc_backend_services.services.AccommodationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        return ResponseEntity.ok(Result.success(200, "酒店发布成功，等待平台审核", accommodationVO));
    }

    @PutMapping("/merchant/updateAccommodation")
    public ResponseEntity<Result<AccommodationVO>> updateAccommodation(
            @RequestParam Long accommodationId,
            @RequestBody AccommodationDTO accommodationDTO) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AccommodationVO accommodationVO = accommodationService.updateAccommodation(accommodationId, accommodationDTO, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "酒店更新成功，等待平台审核", accommodationVO));
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

    @PutMapping("/merchant/addExternalLink")
    public ResponseEntity<Result<ExternalLinkVO>> addExternalLink(
            @RequestParam Long accommodationId,
            @RequestBody ExternalLinkDTO request) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ExternalLinkVO vo = accommodationService.addExternalLink(request, accommodationId, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "外部链接添加成功，等待平台审核", vo));
    }

    @PutMapping("/merchant/updateExternalLink")
    public ResponseEntity<Result<ExternalLinkVO>> updateExternalLink(
            @RequestParam Long externalLinkId,
            @RequestBody ExternalLinkDTO request) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ExternalLinkVO vo = accommodationService.updateExternalLink(externalLinkId, request, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "外部链接更新成功，等待平台审核", vo));
    }

    @DeleteMapping("/merchant/deleteExternalLink")
    public ResponseEntity<Result<String>> deleteExternalLink(@RequestParam Long externalLinkId) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        accommodationService.deleteExternalLink(externalLinkId, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "外部链接删除成功", null));
    }

    @GetMapping("/user/getExternalLinks")
    public ResponseEntity<Result<List<ExternalLinkVO>>> showExternalLinksByUser(@RequestParam Long accommodationId) {
        List<ExternalLinkVO> data = accommodationService.showExternalLinksByUser(accommodationId);
        return ResponseEntity.ok(Result.success(200, "用户端外部链接获取成功", data));
    }

    @GetMapping("/merchant/getExternalLinks")
    public ResponseEntity<Result<List<ExternalLinkVO>>> showExternalLinksByMerchant(@RequestParam Long accommodationId) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ExternalLinkVO> data = accommodationService.showExternalLinksByMerchant(accommodationId, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, "商家端外部链接获取成功", data));
    }

    @PostMapping("/merchant/topLink")
    public ResponseEntity<Result<String>> topALink(@RequestParam Long externalLinkId) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String message = accommodationService.topALink(externalLinkId, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, message, null));
    }

    @PostMapping("/merchant/unTopLink")
    public ResponseEntity<Result<String>> unTopALink(@RequestParam Long externalLinkId) {
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String message = accommodationService.unTopALink(externalLinkId, currentMerchantId);
        return ResponseEntity.ok(Result.success(200, message, null));
    }

    @PutMapping("/approve")
    public ResponseEntity<Result<AccommodationVO>> approveAccommodation(@RequestParam Long accommodationId) {
        AccommodationVO accommodationVO = accommodationService.approveAccommodation(accommodationId, true);
        return ResponseEntity.ok(Result.success(200, "酒店审核通过", accommodationVO));
    }

    @PutMapping("/disApprove")
    public ResponseEntity<Result<AccommodationVO>> disApproveAccommodation(@RequestParam Long accommodationId) {
        AccommodationVO accommodationVO = accommodationService.approveAccommodation(accommodationId, false);
        return ResponseEntity.ok(Result.success(200, "酒店审核不通过", accommodationVO));
    }

    @GetMapping("/all")
    public ResponseEntity<Result<List<Accommodation>>> getAllAccommodations() {
        List<Accommodation> accommodations = accommodationService.getAllAccommodations();
        return ResponseEntity.ok(Result.success(200, "获取住宿列表成功", accommodations));
    }

    @PutMapping("/approveExternalLink")
    public ResponseEntity<Result<ExternalLinkVO>> approveExternalLink(@RequestParam Long externalLinkId) {
        ExternalLinkVO vo = accommodationService.approveExternalLink(externalLinkId, true);
        return ResponseEntity.ok(Result.success(200, "外部链接审核通过", vo));
    }

    @PutMapping("/disApproveExternalLink")
    public ResponseEntity<Result<ExternalLinkVO>> disApproveExternalLink(@RequestParam Long externalLinkId) {
        ExternalLinkVO vo = accommodationService.approveExternalLink(externalLinkId, false);
        return ResponseEntity.ok(Result.success(200, "外部链接审核不通过", vo));
    }

    @GetMapping("/allExternalLinks")
    public ResponseEntity<Result<List<AccommodationExternalLinksVO>>> getAllAccommodationsWithExternalLinks() {
        List<AccommodationExternalLinksVO> data = accommodationService.getAllAccommodationsWithExternalLinks();
        return ResponseEntity.ok(Result.success(200, "获取所有住宿外链信息成功", data));
    }
}
