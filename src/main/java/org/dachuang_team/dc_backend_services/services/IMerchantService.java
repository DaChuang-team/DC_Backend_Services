package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.MerchantResponseDTO;

public interface IMerchantService {

        String registerMerchant(MerchantRegisterDTO merchantRegisterDTO);

        MerchantResponseDTO merchantLogin(String loginID, String password);
}
