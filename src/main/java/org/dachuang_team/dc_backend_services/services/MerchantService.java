package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
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
        if (merchantRepository.findByLoginID(dto.getLoginID()) != null) {
            throw new IllegalArgumentException("账号" + dto.getLoginID() + "已存在");
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

            } else {
                try {
                    // 获取处理后的URL
                    String newUrl = imageProcessUtils.shopBannerImgProcess(img, merchant.getId());
                    // 更新merchant
                    merchant.setBannerUrl(newUrl);
                    // 标记图片已绑定
                    img.setLinked(true);
                    img.setUploadMerchantId(merchant.getId());
                    shopBannerImgRepository.save(img);

                } catch (Exception e) {
                    merchant.setBannerUrl("MERCHANT_DEFAULT_BANNER");
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
}
