package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantLoginDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.services.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @PutMapping("/register")
    public Result<String> registerMerchant(@RequestBody MerchantRegisterDTO merchantRegisterDTO) {
        try {
            String resp = merchantService.registerMerchant(merchantRegisterDTO);
            return Result.success("商户 " + resp + " 注册成功");
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "注册失败，这可能不是你的问题: " + e.getMessage());
        }

    }

    @PostMapping("/login")
    public Result<MerchantVO> loginMerchant(@RequestBody MerchantLoginDTO merchantLoginDTO) {
        try {
            MerchantVO resp = merchantService.merchantLogin(merchantLoginDTO.getLoginID(), merchantLoginDTO.getPassword());
            return Result.success("商户 " + merchantLoginDTO.getLoginID() + " 登录成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "登录失败，这可能不是你的问题: " + e.getMessage());
        }
    }
}
