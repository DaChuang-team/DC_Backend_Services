package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;

public interface IMerchantService {

        String registerMerchant(MerchantRegisterDTO merchantRegisterDTO);

        MerchantVO merchantLogin(String loginID, String password);
}
