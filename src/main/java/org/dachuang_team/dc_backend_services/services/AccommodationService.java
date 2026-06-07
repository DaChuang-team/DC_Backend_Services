package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.ExternalLinkDTO;
import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.Accommodation;
import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.ExternalLink;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.AccommodationImg;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationExternalLinksVO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationImgVO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;
import org.dachuang_team.dc_backend_services.domain.VO.ExternalLinkVO;
import org.dachuang_team.dc_backend_services.repository.AccommodationImgRepository;
import org.dachuang_team.dc_backend_services.repository.AccommodationRepository;
import org.dachuang_team.dc_backend_services.repository.ExternalLinkRepository;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccommodationService implements IAccommodationService {

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private AccommodationImgRepository accommodationImgRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private ImageProcessUtils imageProcessUtils;

    @Autowired
    private ExternalLinkRepository externalLinkRepository;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public AccommodationVO addAccommodation(AccommodationDTO accommodationDTO, Long merchantId) {
        Merchant seller = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        if (seller.getStatus() != 1){
            throw new IllegalStateException("商家未审核通过或被封禁，无法发布酒店");
        }
        if(accommodationDTO.getAccommodationName() == null || accommodationDTO.getAccommodationName().isEmpty()) {
            throw new IllegalArgumentException("酒店名称不能为空");
        }
        if(accommodationDTO.getPriceFrom() == null || accommodationDTO.getPriceFrom() <= 0) {
            throw new IllegalArgumentException("酒店起价必须大于0");
        }
        if(accommodationDTO.getType() == null || accommodationDTO.getType().isEmpty()) {
            throw new IllegalArgumentException("酒店类型不能为空");
        }
        if(accommodationDTO.getPhone() == null || accommodationDTO.getPhone().isEmpty()) {
            throw new IllegalArgumentException("酒店联系电话不能为空");
        }
        if(accommodationDTO.getAddress() == null || accommodationDTO.getAddress().isEmpty()) {
            throw new IllegalArgumentException("酒店地址不能为空");
        }
        if(!accommodationDTO.getType().equals("HOTEL") && !accommodationDTO.getType().equals("HOSTEL") && !accommodationDTO.getType().equals("RESORT")) {
            throw new IllegalArgumentException("酒店类型只能是 HOTEL、HOSTEL 或 RESORT");
        }
        if(accommodationDTO.getLatitude() != null) {
            if (accommodationDTO.getLatitude().compareTo(new BigDecimal("-90")) < 0 ||
                    accommodationDTO.getLatitude().compareTo(new BigDecimal("90")) > 0) {
                throw new IllegalArgumentException("纬度不合法");
            }
        }
        if(accommodationDTO.getLongitude() != null) {
            if (accommodationDTO.getLongitude().compareTo(new BigDecimal("-180")) < 0 ||
                    accommodationDTO.getLongitude().compareTo(new BigDecimal("180")) > 0) {
                throw new IllegalArgumentException("经度不合法");
            }
        }

        Accommodation accommodation = new Accommodation();
        accommodation.setAccommodationName(accommodationDTO.getAccommodationName());
        accommodation.setPhone(accommodationDTO.getPhone());
        accommodation.setType(accommodationDTO.getType());
        accommodation.setDescription(accommodationDTO.getDescription());
        accommodation.setPriceFrom(accommodationDTO.getPriceFrom());
        accommodation.setAmenities(accommodationDTO.getAmenities());
        accommodation.setStarRating(accommodationDTO.getStarRating());
        accommodation.setAddress(accommodationDTO.getAddress());
        accommodation.setLatitude(accommodationDTO.getLatitude());
        accommodation.setLongitude(accommodationDTO.getLongitude());
        accommodation.setCheckInTime(accommodationDTO.getCheckInTime());
        accommodation.setCheckOutTime(accommodationDTO.getCheckOutTime());
        accommodation.setPolicyNote(accommodationDTO.getPolicyNote());
        accommodation.setSeller(seller);
        accommodation.setSellerId(seller.getId());
        accommodation.setPublishedAt(LocalDateTime.now());
        accommodation.setLastModifiedAt(LocalDateTime.now());

        // 先保存主体，拿到 accommodationId
        accommodation = accommodationRepository.save(accommodation);

        List<AccommodationImgVO> imageVOList = new ArrayList<>();
        if (accommodationDTO.getImageIds() != null && !accommodationDTO.getImageIds().isEmpty()) {
            imageVOList = bindAndProcessImages(accommodation.getAccommodationId(), accommodationDTO.getImageIds());

            // 冗余首图缩略图字段，便于列表页展示
            if (!imageVOList.isEmpty()) {
                accommodation.setTbImageUrl(imageVOList.get(0).getThumbnailUrl());
                accommodation = accommodationRepository.save(accommodation);
            }
        }

        AccommodationVO vo = toVO(accommodation);
        vo.setImages(imageVOList);
        return vo;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Void deleteAccommodation(Long accommodationId, Long merchantId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));

        // 只能删除自己的酒店
        if (!accommodation.getSellerId().equals(merchantId)) {
            throw new SecurityException("权限不足：只能删除自己的酒店");
        }

        // 解绑图片
        List<AccommodationImg> oldRecords = accommodationImgRepository.findByAccommodationId(accommodationId);
        for (AccommodationImg rec : oldRecords) {
            rec.setLinked(false);
            rec.setAccommodationId(null);
            rec.setPrimary(false);
            rec.setSortOrder(null);
            accommodationImgRepository.save(rec);
        }

        accommodationRepository.delete(accommodation);
        return null;
    }



    @Override
    @Transactional(rollbackOn = Exception.class)
    public AccommodationVO updateAccommodation(Long accommodationId, AccommodationDTO accommodationDTO, Long merchantId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));

        // 只能修改自己的酒店
        if (!accommodation.getSellerId().equals(merchantId)) {
            throw new SecurityException("权限不足：只能修改自己的酒店");
        }

        // 基本字段更新（仅更新前端传入值）
        if (accommodationDTO.getAccommodationName() != null && !accommodationDTO.getAccommodationName().isEmpty()) {
            accommodation.setAccommodationName(accommodationDTO.getAccommodationName());
        }
        if (accommodationDTO.getPhone() != null && !accommodationDTO.getPhone().isEmpty()) {
            accommodation.setPhone(accommodationDTO.getPhone());
        }
        if (accommodationDTO.getType() != null && !accommodationDTO.getType().isEmpty()) {
            if(!accommodationDTO.getType().equals("HOTEL") && !accommodationDTO.getType().equals("HOSTEL") && !accommodationDTO.getType().equals("RESORT")) {
                throw new IllegalArgumentException("酒店类型只能是 HOTEL、HOSTEL 或 RESORT");
            }
            accommodation.setType(accommodationDTO.getType());
        }
        if (accommodationDTO.getDescription() != null && !accommodationDTO.getDescription().isEmpty()) {
            accommodation.setDescription(accommodationDTO.getDescription());
        }
        if (accommodationDTO.getPriceFrom() != null && accommodationDTO.getPriceFrom() > 0) {
            accommodation.setPriceFrom(accommodationDTO.getPriceFrom());
        }
        if (accommodationDTO.getAmenities() != null && !accommodationDTO.getAmenities().isEmpty()) {
            accommodation.setAmenities(accommodationDTO.getAmenities());
        }
        if (accommodationDTO.getStarRating() != null && accommodationDTO.getStarRating() >= 0 && accommodationDTO.getStarRating() <= 5) {
            accommodation.setStarRating(accommodationDTO.getStarRating());
        }
        if (accommodationDTO.getAddress() != null && !accommodationDTO.getAddress().isEmpty()) {
            accommodation.setAddress(accommodationDTO.getAddress());
        }
        if (accommodationDTO.getLatitude() != null) {
            if (accommodationDTO.getLatitude().compareTo(new BigDecimal("-90")) < 0 ||
                    accommodationDTO.getLatitude().compareTo(new BigDecimal("90")) > 0) {
                throw new IllegalArgumentException("纬度不合法");
            }
            accommodation.setLatitude(accommodationDTO.getLatitude());
        }
        if (accommodationDTO.getLongitude() != null) {
            if (accommodationDTO.getLongitude().compareTo(new BigDecimal("-180")) < 0 ||
                    accommodationDTO.getLongitude().compareTo(new BigDecimal("180")) > 0) {
                throw new IllegalArgumentException("经度不合法");
            }
            accommodation.setLongitude(accommodationDTO.getLongitude());
        }

        if (accommodationDTO.getCheckInTime() != null && !accommodationDTO.getCheckInTime().isEmpty()) {
            accommodation.setCheckInTime(accommodationDTO.getCheckInTime());
        }
        if (accommodationDTO.getCheckOutTime() != null && !accommodationDTO.getCheckOutTime().isEmpty()) {
            accommodation.setCheckOutTime(accommodationDTO.getCheckOutTime());
        }
        if (accommodationDTO.getPolicyNote() != null && !accommodationDTO.getPolicyNote().isEmpty()) {
            accommodation.setPolicyNote(accommodationDTO.getPolicyNote());
        }

        List<AccommodationImgVO> imageVOList = new ArrayList<>();

        // 只有前端传了 imageIds 才认为要更新图片
        if (accommodationDTO.getImageIds() != null) {
            // 先解绑旧图
            List<AccommodationImg> oldRecords = accommodationImgRepository.findByAccommodationId(accommodationId);
            for (AccommodationImg rec : oldRecords) {
                rec.setLinked(false);
                rec.setAccommodationId(null);
                rec.setPrimary(false);
                rec.setSortOrder(null);
                accommodationImgRepository.save(rec);
            }

            // 再按新列表绑定
            if (!accommodationDTO.getImageIds().isEmpty()) {
                imageVOList = bindAndProcessImages(accommodationId, accommodationDTO.getImageIds());
                accommodation.setTbImageUrl(imageVOList.isEmpty() ? null : imageVOList.get(0).getThumbnailUrl());
                accommodation.setApproved(false); // 更新图片后需要重新审核
            } else {
                // 明确传空列表 => 清空图片
                accommodation.setTbImageUrl(null);
            }
        } else {
            // 不更新图片时，返回当前图片列表
            List<AccommodationImg> current = accommodationImgRepository.findByAccommodationId(accommodationId);
            for (AccommodationImg img : current) {
                imageVOList.add(toImgVO(img));
            }
        }

        accommodation.setLastModifiedAt(LocalDateTime.now());
        accommodation = accommodationRepository.save(accommodation);

        AccommodationVO vo = toVO(accommodation);
        vo.setImages(imageVOList);
        return vo;
    }

    // 查询相关

    // 查询单个酒店的详细信息
    public AccommodationVO getAccommodation(Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));

        AccommodationVO vo = toVO(accommodation);

        List<AccommodationImg> images = accommodationImgRepository.findByAccommodationId(accommodationId);
        List<AccommodationImgVO> imageVOList = new ArrayList<>();
        for (AccommodationImg img : images) {
            imageVOList.add(toImgVO(img));
        }
        vo.setImages(imageVOList);
        return vo;
    }

    // 用户搜索酒店，支持分页、关键词、价格过滤、距离排序
    @Override
    public Map<String, Object> searchAccommodationsByUser(String keyword,
                                                          Double minPrice,
                                                          Double latitude,
                                                          Double longitude,
                                                          String type,
                                                          Integer page,
                                                          Integer size) {
        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1 || size > 100) ? 10 : size;

        // 经纬度必须成对出现
        boolean hasLat = latitude != null;
        boolean hasLon = longitude != null;
        if (hasLat ^ hasLon) {
            throw new IllegalArgumentException("经度和纬度必须同时传入");
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("纬度不合法");
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("经度不合法");
        }

        // type 为 null 或空字符串时，不筛选住宿类型，返回所有类型
        if (type == null || type.isEmpty()) {
            type = null;
        } else if (!type.equals("HOTEL") && !type.equals("HOSTEL") && !type.equals("RESORT")) {
            type = null; // 无效的类型参数当作不传处理
        }

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<Object[]> resultPage;

        if (hasLat) {
            resultPage = accommodationRepository.searchSummaryByDistance(
                    keyword, minPrice, latitude, longitude, type, pageable);
        } else {
            resultPage = accommodationRepository.searchSummaryByPublishTime(
                    keyword, minPrice, type, pageable);
        }

        var items = resultPage.getContent().stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("accommodationId", row[0]);
            m.put("accommodationName", row[1]);
            m.put("tbImageUrl", row[2]);
            m.put("priceFrom", row[3]);
            // 传经纬度时才有 distanceKm
            m.put("distanceKm", hasLat ? row[4] : null);
            m.put("amenities", hasLat ? row[5] : row[4]);
            m.put("policyNote", hasLat ? row[6] : row[5]);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("currentPage", resultPage.getNumber() + 1);
        resp.put("pageSize", resultPage.getSize());
        resp.put("totalItems", resultPage.getTotalElements());
        resp.put("totalPages", resultPage.getTotalPages());
        return resp;
    }

    // 商家搜索自己的酒店，支持分页、关键词过滤
    @Override
    public Map<String, Object> searchAccommodationsByMerchant(Long merchantId, String keyword, Integer page, Integer size) {
        int pageNo = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1 || size > 100) ? 10 : size;

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        Page<Accommodation> resultPage;

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            resultPage = accommodationRepository
                    .findBySellerIdAndAccommodationNameContainingIgnoreCaseOrderByPublishedAtDesc(
                            merchantId, keyword.trim(), pageable);
        } else {
            resultPage = accommodationRepository
                    .findBySellerIdOrderByPublishedAtDesc(merchantId, pageable);
        }

        List<Map<String, Object>> items = resultPage.getContent().stream().map(accommodation -> {
            Map<String, Object> m = new HashMap<>();
            m.put("accommodationId", accommodation.getAccommodationId());
            m.put("accommodationName", accommodation.getAccommodationName());
            m.put("tbImageUrl", accommodation.getTbImageUrl());
            m.put("approved", accommodation.getApproved());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("currentPage", resultPage.getNumber() + 1);
        resp.put("pageSize", resultPage.getSize());
        resp.put("totalItems", resultPage.getTotalElements());
        resp.put("totalPages", resultPage.getTotalPages());
        return resp;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ExternalLinkVO addExternalLink(ExternalLinkDTO externalLinkDTO, Long accommodationId, Long merchantId) {
        // 验证酒店存在且属于商家
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));

        if(externalLinkRepository.countByAccommodationId(accommodationId) >= 5) {
            throw new IllegalStateException("每个酒店最多只能添加5个外部链接");
        }
        if (!accommodation.getSellerId().equals(merchantId)) {
            throw new SecurityException("权限不足：只能为自己的酒店添加外部链接");
        }
        if(externalLinkDTO.getPlatform() == null || externalLinkDTO.getPlatform().isEmpty()) {
            throw new IllegalArgumentException("平台名称不能为空");
        }
        if(externalLinkDTO.getUrl() == null || externalLinkDTO.getUrl().isEmpty()) {
            throw new IllegalArgumentException("链接URL不能为空");
        }

        ExternalLink externalLink = new ExternalLink();
        externalLink.setAccommodationId(accommodationId);
        externalLink.setPlatform(externalLinkDTO.getPlatform());
        externalLink.setUrl(externalLinkDTO.getUrl());
        externalLink.setCreatedAt(LocalDateTime.now());
        externalLink.setUpdatedAt(LocalDateTime.now());
        externalLink.setCreatedBy(merchantId);

        externalLink = externalLinkRepository.save(externalLink);

        ExternalLinkVO vo = new ExternalLinkVO();
        vo.setId(externalLink.getId());
        vo.setPlatform(externalLink.getPlatform());
        vo.setUrl(externalLink.getUrl());
        vo.setTopped(externalLink.getTopped());
        vo.setApproved(externalLink.getApproved());

        return vo;
    }

    // 商家查看酒店的外部链接，可以看到所有链接，并且置顶链接排在前面
    @Override
    public List<ExternalLinkVO> showExternalLinksByMerchant(Long accommodationId, Long merchantId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));

        if (!accommodation.getSellerId().equals(merchantId)) {
            throw new SecurityException("权限不足：只能查看自己的酒店的外部链接");
        }

        List<ExternalLink> links = externalLinkRepository.findByAccommodationIdOrderByToppedDesc(accommodationId);
        return links.stream().map(link -> {
            ExternalLinkVO vo = new ExternalLinkVO();
            vo.setId(link.getId());
            vo.setPlatform(link.getPlatform());
            vo.setUrl(link.getUrl());
            vo.setTopped(link.getTopped());
            vo.setApproved(link.getApproved());
            return vo;
        }).collect(Collectors.toList());
    }

    // 用户查看酒店的外部链接，只能看到审核通过的链接
    @Override
    public List<ExternalLinkVO> showExternalLinksByUser(Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));
        if(accommodation.getApproved() == null || !accommodation.getApproved()) {
            throw new IllegalStateException("酒店信息正在审核中，外部链接暂不可见");
        }

        List<ExternalLink> links = externalLinkRepository.findByAccommodationIdAndApprovedTrueOrderByToppedDesc(accommodationId);
        return links.stream().map(link -> {
            ExternalLinkVO vo = new ExternalLinkVO();
            vo.setId(link.getId());
            vo.setPlatform(link.getPlatform());
            vo.setUrl(link.getUrl());
            vo.setTopped(link.getTopped());
            vo.setApproved(link.getApproved());
            return vo;
        }).collect(Collectors.toList());
    }

    // 商家修改外部链接，只能修改自己创建的链接，修改后需要重新审核
    @Override
    @Transactional(rollbackOn = Exception.class)
    public ExternalLinkVO updateExternalLink(Long externalLinkId, ExternalLinkDTO dto, Long merchantId) {
        if (!externalLinkRepository.existsById(externalLinkId)) {
            throw new IllegalArgumentException("外部链接不存在");
        }
        ExternalLink externalLink = externalLinkRepository.findById(externalLinkId).orElseThrow(() -> new IllegalArgumentException("外部链接不存在"));
        if (!Objects.equals(externalLink.getCreatedBy(), merchantId)) {
            throw new SecurityException("权限不足：只能修改自己创建的外部链接");
        }
        if (dto.getPlatform() != null && !dto.getPlatform().isEmpty()) {
            externalLink.setPlatform(dto.getPlatform());
        }
        if (dto.getUrl() != null && !dto.getUrl().isEmpty()) {
            externalLink.setUrl(dto.getUrl());
            externalLink.setApproved(false);
        }

        externalLink.setUpdatedAt(LocalDateTime.now());
        externalLink = externalLinkRepository.save(externalLink);
        ExternalLinkVO vo = new ExternalLinkVO();
        vo.setId(externalLink.getId());
        vo.setPlatform(externalLink.getPlatform());
        vo.setUrl(externalLink.getUrl());
        vo.setTopped(externalLink.getTopped());
        vo.setApproved(externalLink.getApproved());
        return vo;
    }

    // 商家删除外部链接，只能删除自己创建的链接
    @Override
    @Transactional(rollbackOn = Exception.class)
    public Void deleteExternalLink(Long externalLinkId, Long merchantId) {
        Optional<ExternalLink> externalLink = externalLinkRepository.findById(externalLinkId);
        if (externalLink.isEmpty()) {
            throw new IllegalArgumentException("外部链接不存在");
        }
        if(externalLink.get().getCreatedBy() == null || !Objects.equals(externalLink.get().getCreatedBy(), merchantId)) {
            throw new SecurityException("权限不足：只能删除自己创建的外部链接");
        }
        externalLinkRepository.deleteById(externalLinkId);
        return null;
    }

    //置顶一个链接
    @Override
    @Transactional(rollbackOn = Exception.class)
    public String topALink(Long externalLinkId, Long merchantId) {
        ExternalLink link = externalLinkRepository.findById(externalLinkId)
                .orElseThrow(() -> new IllegalArgumentException("外部链接不存在"));
        if(link.getCreatedBy() == null || !Objects.equals(link.getCreatedBy(), merchantId)) {
            throw new SecurityException("权限不足：只能置顶自己创建的外部链接");
        }
        if(link.getTopped() != null && link.getTopped()) {
            return "链接已置顶，无需重复操作";
        }
        // 每个酒店只能有一个置顶链接，如果当前已有置顶链接，则先取消其置顶状态
        if(externalLinkRepository.existsByToppedTrue()){
            externalLinkRepository.resetToppedFalseByAccommodationId(link.getAccommodationId());
        }
        link.setTopped(true);
        externalLinkRepository.save(link);
        if(link.getApproved() == null || !link.getApproved()) {
            return "链接置顶成功，但该链接尚未审核通过，用户端暂不可见";
        }
        return "链接置顶成功";
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String unTopALink(Long externalLinkId, Long merchantId) {
        ExternalLink link = externalLinkRepository.findById(externalLinkId)
                .orElseThrow(() -> new IllegalArgumentException("外部链接不存在"));
        if (link.getCreatedBy() == null || !Objects.equals(link.getCreatedBy(), merchantId)) {
            throw new SecurityException("权限不足：只能取消置顶自己创建的外部链接");
        }
        if (link.getTopped() == null || !link.getTopped()) {
            throw new IllegalStateException("链接未置顶");
        }
        link.setTopped(false);
        externalLinkRepository.save(link);
        return "取消置顶成功";
    }


    // 绑定 + 处理图片（压缩、裁剪、首图缩略图）
    private List<AccommodationImgVO> bindAndProcessImages(Long accommodationId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return new ArrayList<>();
        }
        if (imageIds.size() > 10) {
            throw new IllegalArgumentException("酒店最多支持10张图片");
        }

        List<AccommodationImgVO> result = new ArrayList<>();

        for (int i = 0; i < imageIds.size(); i++) {
            Long recordId = imageIds.get(i);
            AccommodationImg record = accommodationImgRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("图片记录不存在: " + recordId));

            boolean isPrimary = (i == 0);

            // 防止拿别人已绑定的图片直接复用
            if (Boolean.TRUE.equals(record.getLinked())
                    && record.getAccommodationId() != null
                    && !record.getAccommodationId().equals(accommodationId)) {
                throw new IllegalArgumentException("图片已绑定到其他酒店: " + recordId);
            }

            if (!Boolean.TRUE.equals(record.getLinked())) {
                imageProcessUtils.accommodationImgProcessAndCompress(record, accommodationId, i, isPrimary);
            }

            record.setLinked(true);
            record.setAccommodationId(accommodationId);
            record.setSortOrder(i);
            record.setPrimary(isPrimary);

            AccommodationImg saved = accommodationImgRepository.save(record);
            result.add(toImgVO(saved));
        }

        return result;
    }

    private AccommodationVO toVO(Accommodation accommodation) {
        AccommodationVO vo = new AccommodationVO();
        vo.setAccommodationName(accommodation.getAccommodationName());
        vo.setPhone(accommodation.getPhone());
        vo.setType(accommodation.getType());
        vo.setDescription(accommodation.getDescription());
        vo.setPriceFrom(accommodation.getPriceFrom());
        vo.setAmenities(accommodation.getAmenities());
        vo.setStarRating(accommodation.getStarRating());
        vo.setAddress(accommodation.getAddress());
        vo.setLatitude(accommodation.getLatitude());
        vo.setLongitude(accommodation.getLongitude());
        vo.setCheckInTime(accommodation.getCheckInTime());
        vo.setCheckOutTime(accommodation.getCheckOutTime());
        vo.setPolicyNote(accommodation.getPolicyNote());
        vo.setTbImageUrl(accommodation.getTbImageUrl());
        return vo;
    }

    private AccommodationImgVO toImgVO(AccommodationImg img) {
        AccommodationImgVO vo = new AccommodationImgVO();
        vo.setId(img.getId());
        vo.setUrl(img.getUrl());
        vo.setThumbnailUrl(img.getThumbnailUrl());
        vo.setPrimary(img.getPrimary());
        vo.setSortOrder(img.getSortOrder());
        vo.setAccommodationId(img.getAccommodationId());
        return vo;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public AccommodationVO approveAccommodation(Long accommodationId, boolean approved) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new IllegalArgumentException("酒店不存在"));

        accommodation.setApproved(approved);
        accommodation.setLastModifiedAt(LocalDateTime.now());
        accommodation = accommodationRepository.save(accommodation);

        AccommodationVO vo = toVO(accommodation);

        List<AccommodationImg> images = accommodationImgRepository.findByAccommodationId(accommodationId);
        List<AccommodationImgVO> imageVOList = new ArrayList<>();
        for (AccommodationImg img : images) {
            imageVOList.add(toImgVO(img));
        }
        vo.setImages(imageVOList);
        return vo;
    }

    @Override
    public List<Accommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ExternalLinkVO approveExternalLink(Long externalLinkId, boolean approved) {
        ExternalLink externalLink = externalLinkRepository.findById(externalLinkId)
                .orElseThrow(() -> new IllegalArgumentException("外部链接不存在"));

        externalLink.setApproved(approved);
        externalLink.setUpdatedAt(LocalDateTime.now());
        externalLink = externalLinkRepository.save(externalLink);

        ExternalLinkVO vo = new ExternalLinkVO();
        vo.setId(externalLink.getId());
        vo.setPlatform(externalLink.getPlatform());
        vo.setUrl(externalLink.getUrl());
        vo.setTopped(externalLink.getTopped());
        vo.setApproved(externalLink.getApproved());

        return vo;
    }

    @Override
    public List<AccommodationExternalLinksVO> getAllAccommodationsWithExternalLinks() {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        List<AccommodationExternalLinksVO> result = new ArrayList<>();

        for (Accommodation acc : accommodations) {
            AccommodationExternalLinksVO vo = new AccommodationExternalLinksVO();
            vo.setAccommodationId(acc.getAccommodationId().toString());
            vo.setAccommodationName(acc.getAccommodationName());
            vo.setType(acc.getType());
            vo.setAddress(acc.getAddress());

            List<ExternalLink> links = externalLinkRepository.findByAccommodationIdOrderByToppedDesc(acc.getAccommodationId());
            List<ExternalLinkVO> linkVOs = links.stream().map(link -> {
                ExternalLinkVO linkVO = new ExternalLinkVO();
                linkVO.setId(link.getId());
                linkVO.setPlatform(link.getPlatform());
                linkVO.setUrl(link.getUrl());
                linkVO.setTopped(link.getTopped());
                linkVO.setApproved(link.getApproved());
                return linkVO;
            }).collect(Collectors.toList());

            vo.setExternalLinks(linkVOs);
            result.add(vo);
        }

        return result;
    }
}
