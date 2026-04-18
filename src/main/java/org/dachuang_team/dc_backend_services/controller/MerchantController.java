package org.dachuang_team.dc_backend_services.controller;

import jakarta.validation.Valid;
import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantLoginDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantRegisterDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.MerchantUpdateDTO;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;
import org.dachuang_team.dc_backend_services.services.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @PostMapping("/register/smsSend")
    public Result<String> sendRegistrationCode(@RequestParam @Valid String merchantPhone) {
        try {
            merchantService.sendVerificationCode(merchantPhone, SmsScene.REGISTER);
            return Result.success("验证码发送成功");
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "验证码发送失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/info/check")
    public Result<String> checkMerchantInfo(
            @RequestParam(required = false) String merchantPhone,
            @RequestParam(required = false) String loginID,
            @RequestParam(required = false) String shopName) {
        try {
            String resp = merchantService.infoCheck(merchantPhone, loginID, shopName);
            return Result.success(resp);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "信息检查失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/register/confirm")
    public Result<String> register(
            @RequestBody MerchantRegisterDTO dto,
            @RequestParam String code){
        try{
            String resp = merchantService.registerMerchant(dto, code);
            return Result.success(resp);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "注册失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/login/smsSend")
    public Result<String> sendLoginCode(@RequestParam @Valid String merchantPhone) {
        try {
            merchantService.sendVerificationCode(merchantPhone, SmsScene.LOGIN);
            return Result.success("验证码发送成功");
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "验证码发送失败，这可能不是你的问题: " + e.getMessage());
        }
    }


    @PostMapping("/login/byPassword")
    public Result<MerchantVO> loginMerchant(@RequestBody MerchantLoginDTO merchantLoginDTO) {
        try {
            MerchantVO resp = merchantService.merchantLoginByPassword(merchantLoginDTO);
            return Result.success("商户 " + merchantLoginDTO.getLoginID() + " 登录成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "登录失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/login/bySms")
    public Result<MerchantVO> loginMerchantBySms(@RequestParam String merchantPhone, @RequestParam String code) {
        try {
            MerchantVO resp = merchantService.merchantLoginBySms(merchantPhone, code);
            return Result.success("商户 " + merchantPhone + " 登录成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "登录失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<Map<String, Object>> updateMerchant(@RequestBody MerchantUpdateDTO merchantUpdateDTO) {
        try {
            Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> resp = merchantService.updateMerchant(merchantUpdateDTO, currentMerchantId);
            return Result.success("商户信息更新成功", resp);
        }  catch (IllegalArgumentException e) {
        return Result.error(402, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "商户信息更新失败，这可能不是你的问题: " + e.getMessage(), null);
        }
    }

}
