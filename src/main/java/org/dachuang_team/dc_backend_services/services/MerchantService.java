package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.domain.DTO.*;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ShopBannerImg;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
import org.dachuang_team.dc_backend_services.repository.ShopBannerImgRepository;
import org.jetbrains.annotations.NotNull;
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

    @Autowired
    private SmsCodeService smsService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private SmsCodeService smsCodeService;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String registerMerchant(MerchantRegisterDTO dto, String code){

        boolean isValidCode = smsCodeService.verifyCode(dto.getMerchantPhone(), code, SmsScene.REGISTER);
        if(!isValidCode){
            throw new IllegalArgumentException("验证码错误或已过期");
        }
        // 校验唯一参数
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
        if(merchantRepository.existsByMerchantPhone(dto.getMerchantPhone())){
            throw new IllegalArgumentException("手机号" + dto.getMerchantPhone() + "已被注册");
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

        return "商户账号：" + merchant.getLoginID() + ",手机号：" + merchant.getMerchantPhone() + " 注册完毕，请等待系统审核";
    }

    @Override
    public String infoCheck(String merchantPhone, String loginID, String shopName) {
        int count = 0;
        if (merchantPhone != null && !merchantPhone.isEmpty()) count++;
        if (loginID != null && !loginID.isEmpty()) count++;
        if (shopName != null && !shopName.isEmpty()) count++;

        if (count > 1) {
            throw new IllegalArgumentException("每次只能检查一个参数");
        }

        if (merchantPhone != null && !merchantPhone.isEmpty()) {
            if (merchantRepository.existsByMerchantPhone(merchantPhone)) {
                return "手机号已被注册";
            }
            return "OK";
        }
        if (loginID != null && !loginID.isEmpty()) {
            if (merchantRepository.existsByLoginID(loginID)) {
                return "账号已存在";
            }
            return "OK";
        }
        if (shopName != null && !shopName.isEmpty()) {
            if (merchantRepository.existsByShopName(shopName)) {
                return "店铺名称已存在";
            }
            return "OK";
        }
        throw new IllegalArgumentException("未提供参数或参数为空");
    }


    @Override
    public String sendVerificationCode(String merchantPhone, SmsScene scene) {
        // 如果是注册或者换绑手机号场景，校验手机号必须未被注册过；如果是登录、忘记密码或验证绑定手机号场景，校验手机号必须已经注册过
        if (scene == SmsScene.REGISTER || scene == SmsScene.CHECK_NEW_PHONE) {
            if (merchantRepository.existsByMerchantPhone(merchantPhone)) {
                throw new IllegalArgumentException("手机号已被注册");
            }
        } else {
            if (!merchantRepository.existsByMerchantPhone(merchantPhone)) {
                throw new IllegalArgumentException("手机号未注册");
            }
        }
        smsService.sendCode(merchantPhone, scene);
        return "验证码已发送至" + maskPhone(merchantPhone);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public MerchantVO merchantLoginByPassword(MerchantLoginDTO dto) {
        try {
            Merchant merchant = null;

            // 优先手机号+密码登录
            if (dto.getMerchantPhone() != null && !dto.getMerchantPhone().isEmpty()
                    && dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                if(!merchantRepository.existsByMerchantPhone(dto.getMerchantPhone())){
                    throw new IllegalArgumentException("注册手机号不存在");
                }
                merchant = merchantRepository.findByMerchantPhone(dto.getMerchantPhone());
                if (!passwordEncoder.matches(dto.getPassword(), merchant.getPassword())) {
                    throw new IllegalArgumentException("密码错误");
                }
            }
            // 登录ID+密码登录
            else if (dto.getLoginID() != null && !dto.getLoginID().isEmpty()
                    && dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                if(!merchantRepository.existsByLoginID(dto.getLoginID())){
                    throw new IllegalArgumentException("登录账号不存在");
                }
                merchant = merchantRepository.findByLoginID(dto.getLoginID());
                if (!passwordEncoder.matches(dto.getPassword(), merchant.getPassword())) {
                    throw new IllegalArgumentException("密码错误");
                }
            } else {
                throw new IllegalArgumentException("登录账号/手机号和密码不能为空");
            }

            if (merchant.getStatus() == 3) {
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
    public MerchantVO merchantLoginBySms(String merchantPhone, String code) {
        try {
            if (!merchantRepository.existsByMerchantPhone(merchantPhone)) {
                throw new IllegalArgumentException("注册手机号不存在");
            }
            boolean isValidCode = smsCodeService.verifyCode(merchantPhone, code, SmsScene.LOGIN);
            if (!isValidCode) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
            Merchant merchant = merchantRepository.findByMerchantPhone(merchantPhone);
            if (merchant.getStatus() == 3) {
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
    public Map<String, Object> updateMerchantPwd(MerchantPwUpdateDTO dto, Long merchantId, String merchantPhone) {
        try {
            Merchant merchant;

            // merchantId优先；否则用merchantPhone定位商家
            if (merchantId != null) {
                merchant = merchantRepository.findById(merchantId)
                        .orElseThrow(() -> new IllegalArgumentException("商户不存在"));
            } else if (merchantPhone != null && !merchantPhone.isBlank()) {
                merchant = merchantRepository.findByMerchantPhone(merchantPhone);
                if (merchant == null) {
                    throw new IllegalArgumentException("商户不存在");
                }
            } else {
                throw new IllegalArgumentException("商户标识缺失");
            }

            if (merchant.getStatus() == 3) {
                throw new IllegalArgumentException("账号已被封禁，无法修改密码");
            }

            boolean hasOldPwdFlow = dto.getOldPassword() != null && !dto.getOldPassword().isBlank();
            boolean hasSmsFlow = dto.getCode() != null && !dto.getCode().isBlank();

            if (hasOldPwdFlow && hasSmsFlow) {
                throw new IllegalArgumentException("旧密码和验证码模式不能同时使用");
            }
            if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
                throw new IllegalArgumentException("新密码不能为空");
            }

            // 已登录：旧密码改密
            if (hasOldPwdFlow) {
                if (!passwordEncoder.matches(dto.getOldPassword(), merchant.getPassword())) {
                    throw new IllegalArgumentException("旧密码错误");
                }
            }
            // 免登录：短信验证码改密
            else if (hasSmsFlow) {
                boolean isValidCode = smsCodeService.verifyCode(
                        merchant.getMerchantPhone(), dto.getCode(), SmsScene.RESET_PWD);
                if (!isValidCode) {
                    throw new IllegalArgumentException("验证码错误或已过期");
                }
            } else {
                throw new IllegalArgumentException("参数错误：请选择旧密码方式或验证码方式");
            }

            merchant.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);

            return getMerchantNormalMapResult(merchant);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("密码更新失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public Map<String, Object> updateMerchantPhone(MerchantPhoneUpdateDTO dto, Long merchantId){
        try {
            Merchant merchant = merchantRepository.findById(merchantId)
                    .orElseThrow(() -> new IllegalArgumentException("商户不存在"));
            if (merchant.getStatus() == 3) {
                throw new IllegalArgumentException("账号已被封禁，无法修改信息");
            }
            // 校验新手机号是否已被注册
            if (merchantRepository.existsByMerchantPhone(dto.getNewPhone())) {
                throw new IllegalArgumentException("手机号" + dto.getNewPhone() + "已被注册");
            }
            // 新手机号与原手机号不能相同
            if (dto.getNewPhone().equals(merchant.getMerchantPhone())) {
                throw new IllegalArgumentException("新手机号不能与原手机号相同");
            }
            // 校验验证码
            boolean isOldCodeValid = smsCodeService.verifyCode(merchant.getMerchantPhone(), dto.getOldPhoneVerifyCode(), SmsScene.CHECK_OLD_PHONE);
            if (!isOldCodeValid) {
                throw new IllegalArgumentException("旧手机号验证码错误或已过期");
            }
            boolean isNewCodeValid = smsCodeService.verifyCode(dto.getNewPhone(), dto.getNewPhoneVerifyCode(), SmsScene.CHECK_NEW_PHONE);
            if (!isNewCodeValid) {
                throw new IllegalArgumentException("新手机号验证码错误或已过期");
            }

            merchant.setMerchantPhone(dto.getNewPhone());
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantRepository.save(merchant);

            return getMerchantNormalMapResult(merchant);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("手机号更新失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public Map<String, Object> updateMerchantNormalFields(MerchantUpdateDTO dto, Long merchantId){
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("商户不存在"));
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
        if(dto.getLoginID() != null && !dto.getLoginID().isEmpty()){
            if (merchantRepository.existsByLoginID(dto.getLoginID())) {
                throw new IllegalArgumentException("账号" + dto.getLoginID() + "已存在");
            }
            merchant.setLoginID(dto.getLoginID());
        }
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantRepository.save(merchant);

        return getMerchantNormalMapResult(merchant);
    }

    @NotNull
    private Map<String, Object> getMerchantNormalMapResult(Merchant merchant) {

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

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
