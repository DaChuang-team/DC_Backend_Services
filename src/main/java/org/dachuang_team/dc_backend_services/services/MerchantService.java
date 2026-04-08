package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.pojo.Dto.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.MerchantResponseDTO;
import org.dachuang_team.dc_backend_services.pojo.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.pojo.UserPO.UserGeneral;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String registerMerchant (MerchantRegisterDTO merchantRegisterDTO){
        try {
            Merchant existingMerchant = merchantRepository.findByLoginID(merchantRegisterDTO.getLoginID());
            if (existingMerchant != null) {
                throw new IllegalArgumentException("账号" + merchantRegisterDTO.getLoginID() + "已存在");
            }


            // 允许为空字段自动处理
            if(merchantRegisterDTO.getBannerUrl() == null || merchantRegisterDTO.getBannerUrl().isEmpty()){
                //TODO: 从系统图片资源库中获取一张MERCHANT_DEFAULT_BANNER标签的图片绑定至用户信息里
                // 或者直接添加该标识让前端识别从前端APP中直接获取降低后端服务器压力
                merchantRegisterDTO.setBannerUrl("MERCHANT_DEFAULT_BANNER");
            }
            if(merchantRegisterDTO.getDescription() == null || merchantRegisterDTO.getDescription().isEmpty()){
                merchantRegisterDTO.setDescription("这个商户很懒，什么都没有留下");
            }

            String EncryptedPassword = passwordEncoder.encode(merchantRegisterDTO.getPassword());
            merchantRegisterDTO.setPassword(EncryptedPassword);

            Merchant merchant = new Merchant();
            BeanUtils.copyProperties(merchantRegisterDTO, merchant);
            merchant.setCreatedAt(LocalDateTime.now());
            merchant.setUpdatedAt(LocalDateTime.now());
            merchant.setStatus(0);
            merchantRepository.save(merchant);

            return merchantRegisterDTO.getLoginID();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public MerchantResponseDTO merchantLogin(String loginID, String password){
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

            MerchantResponseDTO merchantResponseDTO = new MerchantResponseDTO();
            BeanUtils.copyProperties(merchant, merchantResponseDTO);
            merchantResponseDTO.setToken(token);

            return merchantResponseDTO;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("登录时发生服务器错误");
        }
    }
}
