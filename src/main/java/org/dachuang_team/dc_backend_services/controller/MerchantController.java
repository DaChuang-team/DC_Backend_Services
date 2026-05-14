package org.dachuang_team.dc_backend_services.controller;

import jakarta.validation.Valid;
import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.*;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.domain.VO.MerchantVO;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
import org.dachuang_team.dc_backend_services.services.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private MerchantRepository merchantRepository;

    @PostMapping("/register/smsSend")
    public Result<String> sendRegistrationCode(@RequestParam @Valid String merchantPhone) {
        try {
            String resp = merchantService.sendVerificationCode(merchantPhone, SmsScene.REGISTER);
            return Result.success("验证码发送成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
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
            return Result.error(400, "参数错误: " + e.getMessage(), null);
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
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "注册失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/login/smsSend")
    public Result<String> sendLoginCode(@RequestParam @Valid String merchantPhone) {
        try {
            String resp = merchantService.sendVerificationCode(merchantPhone, SmsScene.LOGIN);
            return Result.success("验证码发送成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "验证码发送失败，这可能不是你的问题: " + e.getMessage());
        }
    }


    @PostMapping("/login/byPassword")
    public Result<MerchantVO> loginMerchantByPw(@RequestBody MerchantLoginDTO merchantLoginDTO) {
        try {
            MerchantVO resp = merchantService.merchantLoginByPassword(merchantLoginDTO);
            return Result.success("商户 " + (merchantLoginDTO.getLoginID() == null ? merchantLoginDTO.getMerchantPhone() : merchantLoginDTO.getLoginID()) + " 登录成功", resp);

        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
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
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "登录失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/resetPwSmsSend")
    public Result<String> resetPwSmsSend(@RequestParam String merchantPhone){
        try {
            String resp = merchantService.sendVerificationCode(merchantPhone, SmsScene.RESET_PWD);
            return Result.success("验证码发送成功", resp);
        }  catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "验证码发送失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/resetPwBySms")
    public Result<Map<String, Object>> resetPasswordBySms(
            @RequestParam String merchantPhone,
            @RequestBody @Valid MerchantPwUpdateDTO dto) {
        try {
            Map<String, Object> resp = merchantService.updateMerchantPwd(dto, null, merchantPhone);
            return Result.success("密码更新成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "密码更新失败，这可能不是你的问题: " + e.getMessage(), null);
        }
    }

    @PostMapping("/update/resetPwByOldPw")
    public Result<Map<String, Object>> resetPasswordByOldPw(@RequestBody @Valid MerchantPwUpdateDTO dto) {
        try {
            Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentMerchantId == null) {
                return Result.error(401, "未认证，无法修改密码", null);
            }
            Map<String, Object> resp = merchantService.updateMerchantPwd(dto, currentMerchantId, null);
            return Result.success("密码更新成功", resp);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "密码更新失败，这可能不是你的问题: " + e.getMessage(), null);
        }
    }

    @PostMapping("/update/oldPhoneSmsSend")
    public Result<String> oldPhoneSmsSend(){
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Merchant merchant = merchantRepository.findMerchantById(currentMerchantId);
            if(merchant == null){
                return Result.error(401, "商户不存在", null);
            }
            String resp = merchantService.sendVerificationCode(merchant.getMerchantPhone(), SmsScene.CHECK_OLD_PHONE);
            return Result.success("验证码发送成功", resp);
        }  catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "验证码发送失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/update/newPhoneSmsSend")
    public Result<String> newPhoneSmsSend(@RequestParam String newPhone){
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Merchant merchant = merchantRepository.findMerchantById(currentMerchantId);
            if(merchant == null){
                return Result.error(401, "商户不存在", null);
            }
            if(merchant.getMerchantPhone().equals(newPhone)){
                return Result.error(400,"新手机号不能与旧手机号相同", null);
            }
            String resp = merchantService.sendVerificationCode(newPhone, SmsScene.CHECK_NEW_PHONE);
            return Result.success("验证码发送成功", resp);
        }  catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "验证码发送失败，这可能不是你的问题: " + e.getMessage());
        }
    }

    @PostMapping("/update/resetPhoneConfirm")
    public Result<Map<String, Object>> resetPhone(@RequestBody MerchantPhoneUpdateDTO dto){
        try {
            Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> resp = merchantService.updateMerchantPhone(dto, currentMerchantId);
            return Result.success("手机号更新成功", resp);
        }  catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "手机号更新失败，这可能不是你的问题: " + e.getMessage(), null);
        }
    }

    @PutMapping("/update/normal")
    public Result<Map<String, Object>> updateMerchant(@RequestBody MerchantUpdateDTO merchantUpdateDTO) {
        try {
            Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> resp = merchantService.updateMerchantNormalFields(merchantUpdateDTO, currentMerchantId);
            return Result.success("商户信息更新成功", resp);
        }  catch (IllegalArgumentException e) {
        return Result.error(400, "参数错误" + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "商户信息更新失败，这可能不是你的问题: " + e.getMessage(), null);
        }
    }

    @GetMapping("/info")
    public Result<MerchantVO> getMyInfo() {
        try {
            Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            MerchantVO merchantVO = merchantService.getMerchantInfo(currentMerchantId);
            return Result.success("获取商户信息成功", merchantVO);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取商户信息失败，这可能不是你的问题: " + e.getMessage(), null);
        }
    }
}
