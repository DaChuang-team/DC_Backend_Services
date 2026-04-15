package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.domain.DTO.AccommodationDTO;
import org.dachuang_team.dc_backend_services.domain.PO.AccommodationPO.Accommodation;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.AccommodationImg;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationImgVO;
import org.dachuang_team.dc_backend_services.domain.VO.AccommodationVO;
import org.dachuang_team.dc_backend_services.repository.AccommodationImgRepository;
import org.dachuang_team.dc_backend_services.repository.AccommodationRepository;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    @Transactional
    public AccommodationVO addAccommodation(AccommodationDTO accommodationDTO, Long merchantId) {
        Merchant seller = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商家不存在"));

        // 与 ProductService 逻辑保持一致：状态为 0 时不允许发布
        if (seller.getStatus() != 1){
            throw new IllegalStateException("商家未审核通过或被封禁，无法发布酒店");
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
}
