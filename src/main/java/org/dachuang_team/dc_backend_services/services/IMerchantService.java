package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.MerchantLoginDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantUpdateDTO;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;

import java.util.Map;

public interface IMerchantService {

        String registerMerchant(MerchantRegisterDTO dto, String code);

        MerchantVO merchantLoginByPassword(MerchantLoginDTO dto);

        MerchantVO merchantLoginBySms(String merchantPhone, String code);

        String infoCheck(String merchantPhone, String loginID, String shopName);

        Map<String, Object> updateMerchant(MerchantUpdateDTO merchantUpdateDTO, Long id);

        void sendVerificationCode(String merchantPhone, SmsScene scene);
}
