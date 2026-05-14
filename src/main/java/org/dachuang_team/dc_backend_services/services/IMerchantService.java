package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.*;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantAdminVO;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;

import java.util.List;
import java.util.Map;

public interface IMerchantService {

        String registerMerchant(MerchantRegisterDTO dto, String code);

        MerchantVO merchantLoginByPassword(MerchantLoginDTO dto);

        MerchantVO merchantLoginBySms(String merchantPhone, String code);

        String infoCheck(String merchantPhone, String loginID, String shopName);

        Map<String, Object> updateMerchantNormalFields(MerchantUpdateDTO merchantUpdateDTO, Long id);

        Map<String, Object> updateMerchantPwd(MerchantPwUpdateDTO dto, Long merchantId, String merchantPhone);

        Map<String, Object> updateMerchantPhone(MerchantPhoneUpdateDTO dto, Long merchantId);

        String sendVerificationCode(String merchantPhone, SmsScene scene);

        MerchantVO getMerchantInfo(Long merchantId);

        List<MerchantAdminVO> getAllMerchants();

        boolean updateMerchantStatusByAdmin(Long merchantId, Integer status);
}
