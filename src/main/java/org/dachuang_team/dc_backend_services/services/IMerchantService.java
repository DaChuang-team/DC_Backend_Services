package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantUpdateDTO;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;

import java.util.Map;

public interface IMerchantService {

        String registerMerchant(MerchantRegisterDTO merchantRegisterDTO);

        MerchantVO merchantLogin(String loginID, String password);

        Map<String, Object> updateMerchant(MerchantUpdateDTO merchantUpdateDTO, Long id);
}
