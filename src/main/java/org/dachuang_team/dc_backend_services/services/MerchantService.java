package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantUpdateDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ShopBannerImg;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
import org.dachuang_team.dc_backend_services.repository.ShopBannerImgRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MerchantService implements IMerchantService{

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ImageProcessUtils imageProcessUtils;

    @Autowired
    private ShopBannerImgRepository shopBannerImgRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String registerMerchant(MerchantRegisterDTO dto){
        // 校验账号
        if (merchantRepository.existsByLoginID(dto.getLoginID())) {
            throw new IllegalArgumentException("账号" + dto.getLoginID() + "已存在");
        }

        if (dto.getShopName() == null || dto.getShopName().isEmpty()) {
            String randomNum = String.format("%04d", new Random().nextInt(10000));
            dto.setShopName(dto.getMerchantName() + "的店铺" + randomNum);
        } else {
            if(merchantRepository.existsByShopName(dto.getShopName())){
                throw new IllegalArgumentException("店铺名称" + dto.getShopName() + "已存在");
            }
        }

        // 默认值处理
        if (dto.getBannerUrl() == null || dto.getBannerUrl().isEmpty()) {
            dto.setBannerUrl("MERCHANT_DEFAULT_BANNER");
        }

        if (dto.getDescription() == null || dto.getDescription().isEmpty()) {
            dto.setDescription("这个商户很懒，什么都没有留下");
        }

        // 构建实体
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(dto, merchant);

        merchant.setPassword(passwordEncoder.encode(dto.getPassword()));
        merchant.setCreatedAt(LocalDateTime.now());
        merchant.setUpdatedAt(LocalDateTime.now());
        merchant.setStatus(0);

        // 先保存，拿到ID
        merchantRepository.save(merchant);

        // 处理BannerImg
        if (!"MERCHANT_DEFAULT_BANNER".equals(merchant.getBannerUrl())) {

            ShopBannerImg img = shopBannerImgRepository.findByImgUrl(merchant.getBannerUrl());
            if (img == null) {
                // 如果找不到对应的图片记录，降级为默认Banner
                merchant.setBannerUrl("MERCHANT_DEFAULT_BANNER");
                merchantRepository.save(merchant);
            } else {
                try {
                    // 获取处理后的URL
                    String newUrl = imageProcessUtils.shopBannerImgProcess(img, merchant.getId());
                    // 更新merchant
                    merchant.setBannerUrl(newUrl);
                    merchantRepository.save(merchant);
                    // 标记图片已绑定
                    img.setLinked(true);
                    img.setUploadMerchantId(merchant.getId());
                    shopBannerImgRepository.save(img);

                } catch (Exception e) {
                    merchant.setBannerUrl("MERCHANT_DEFAULT_BANNER");
                    merchantRepository.save(merchant);
                }
            }
        }


        return merchant.getLoginID();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public MerchantVO merchantLogin(String loginID, String password){
        try {
            Merchant merchant = merchantRepository.findByLoginID(loginID);
            if (merchant == null) {
                throw new IllegalArgumentException("账号不存在");
            }
            if (!passwordEncoder.matches(password, merchant.getPassword())) {
                throw new IllegalArgumentException("密码错误");
            }
            if(merchant.getStatus() == 3){
                throw new IllegalArgumentException("账号已被封禁，请联系管理员");
            }

            // 更新最后登录时间
            LocalDateTime now = LocalDateTime.now();
            merchant.setLastLoginAt(now);
            merchantRepository.save(merchant);

            // 生成并存储Token
            String token = authService.generateToken(merchant.getId(), "MERCHANT");

            MerchantVO merchantVO = new MerchantVO();
            BeanUtils.copyProperties(merchant, merchantVO);
            merchantVO.setToken(token);

            return merchantVO;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("登录时发生服务器错误");
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Map<String, Object> updateMerchant(MerchantUpdateDTO dto, Long merchantId){
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商户不存在"));
        if(merchant.getUpdatedAt() != null && merchant.getUpdatedAt().plusDays(7).isAfter(LocalDateTime.now())){
            throw new IllegalArgumentException("7天内只能修改一次商户信息");
        }
        if(merchant.getStatus() == 3){
            throw new IllegalArgumentException("账号已被封禁，无法修改信息");
        }

        if(dto.getBannerUrl() != null && !dto.getBannerUrl().isEmpty()){
            if("MERCHANT_DEFAULT_BANNER".equals(dto.getBannerUrl())){
                merchant.setBannerUrl("MERCHANT_DEFAULT_BANNER");
                List<ShopBannerImg> linkedImgs = shopBannerImgRepository.findByUploadMerchantId(merchantId);
                if(linkedImgs != null){
                    for(ShopBannerImg img : linkedImgs){
                        img.setLinked(false);
                        shopBannerImgRepository.save(img);
                    }
                }
            } else {
                ShopBannerImg img = shopBannerImgRepository.findByImgUrl(dto.getBannerUrl());
                if (img == null) {
                    throw new IllegalArgumentException("无效的Banner URL");
                }
                if(img.getUploadMerchantId() != null && !img.getUploadMerchantId().equals(merchantId)){
                    throw new IllegalArgumentException("该Banner已被其他商户使用");
                }

                try {
                    String newUrl = imageProcessUtils.shopBannerImgProcess(img, merchant.getId());
                    merchant.setBannerUrl(newUrl);
                    img.setLinked(true);
                    img.setUploadMerchantId(merchant.getId());
                    shopBannerImgRepository.save(img);
                } catch (Exception e) {
                    throw new RuntimeException("处理Banner图片时发生错误");
                }
            }

        }
        if (dto.getDescription() != null) {
            if (dto.getDescription().isEmpty()) {
                merchant.setDescription("这个商户很懒，什么都没有留下");
            } else {
                merchant.setDescription(dto.getDescription());
            }
        }

        if(dto.getShopName() != null && !dto.getShopName().isEmpty()){
            if(merchantRepository.existsByShopName(dto.getShopName())){
                throw new IllegalArgumentException("店铺名称" + dto.getShopName() + "已存在");
            }
            merchant.setShopName(dto.getShopName());
        }

        if(dto.getMerchantAddress() != null&& !dto.getMerchantAddress().isEmpty()) {
            merchant.setMerchantAddress(dto.getMerchantAddress());
        }
        if(dto.getMerchantName() != null&& !dto.getMerchantName().isEmpty()){
            merchant.setMerchantName(dto.getMerchantName());
        }
        if(dto.getMerchantPhone() != null&& !dto.getMerchantPhone().isEmpty()){
            merchant.setMerchantPhone(dto.getMerchantPhone());
        }
        if (dto.getOldPassword() != null && !dto.getOldPassword().isEmpty()) {
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                throw new IllegalArgumentException("新密码不能为空");
            }
            if (!passwordEncoder.matches(dto.getOldPassword(), merchant.getPassword())) {
                throw new IllegalArgumentException("旧密码错误");
            }
            merchant.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if(dto.getLoginID() != null && !dto.getLoginID().isEmpty()){
            if (merchantRepository.existsByLoginID(dto.getLoginID())) {
                throw new IllegalArgumentException("账号" + dto.getLoginID() + "已存在");
            }
            merchant.setLoginID(dto.getLoginID());
        }
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);

        Map<String, Object> result = new HashMap<>();
        result.put("merchantName", merchant.getMerchantName());
        result.put("merchantAddress", merchant.getMerchantAddress());
        result.put("merchantPhone", merchant.getMerchantPhone());
        result.put("shopName", merchant.getShopName());
        result.put("loginID", merchant.getLoginID());
        result.put("description", merchant.getDescription());
        result.put("bannerUrl", merchant.getBannerUrl());
        return result;
    }
}
