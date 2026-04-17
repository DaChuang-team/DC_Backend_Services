package org.dachuang_team.dc_backend_services.services;

import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;
import org.dachuang_team.dc_backend_services.services.ServiceException.SmsVerifyException;
import org.dachuang_team.dc_backend_services.services.ServiceException.SmsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.aliyun.dypnsapi20170525.Client;

import java.util.Map;

@Service
public class SmsCodeService implements ISmsCodeService {

    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);

    private final String signName = "速通互联验证码";

    private final Client aliyunSmsClient;

    public SmsCodeService(Client aliyunSmsClient) {
        this.aliyunSmsClient = aliyunSmsClient;
    }

    @Override
    public String sendCode(String phone, SmsScene scene) {
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName(signName)
                .setSchemeName(scene.getSchemeName())
                .setTemplateCode(scene.getTemplateCode())
                .setTemplateParam(scene.getTemplateParam())
                .setValidTime(scene.getValidTime())
                .setInterval(scene.getInterval())
                .setCodeLength(scene.getCodeLength())
                .setCodeType(scene.getCodeType())
                .setDuplicatePolicy(scene.getDuplicatePolicy())
                .setCountryCode("86")
                .setReturnVerifyCode(false);

        try {
            SendSmsVerifyCodeResponse response = aliyunSmsClient.sendSmsVerifyCode(request);
            var body = response.getBody();

            if (!"OK".equals(body.getCode()) || !Boolean.TRUE.equals(body.getSuccess())) {
                log.error("[SMS] 发送失败 phone={} scene={} code={} message={}",
                        maskPhone(phone), scene, body.getCode(), body.getMessage());
                throw new SmsException(mapSendErrorCode(body.getCode()));
            }

            String bizId = body.getModel().getBizId();

            log.info("[SMS] 发送成功 phone={} scene={} bizId={}", maskPhone(phone), scene, bizId);
            return bizId;

        } catch (SmsException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SMS] 发送异常 phone={} scene={}", maskPhone(phone), scene, e);
            throw new SmsException("短信发送失败，请稍后重试");
        }
    }

    @Override
    public Boolean verifyCode(String phone, String code, SmsScene scene) {
        CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                .setSchemeName(scene.getSchemeName())
                .setPhoneNumber(phone)
                .setVerifyCode(code);

        try {
            CheckSmsVerifyCodeResponse response =
                    aliyunSmsClient.checkSmsVerifyCode(request);
            var body = response.body;

            if (body == null) {
                throw new SmsVerifyException("验证码校验异常，请重试");
            }

            if(!body.success){
                throw new SmsVerifyException("验证码校验请求失败，请重试");
            }

            Map<String, Object> model = body.model != null ? body.model.toMap() : null;
            log.info("[SMS] 验证结果 phone={} scene={} verifyResult={} model={}",
                    maskPhone(phone), scene,
                    body.model != null ? body.model.verifyResult : "null",
                    model != null ? model.toString() : "null");

            // verifyResult为PASS才是验证通过
            if (body.model == null || !"PASS".equals(body.model.verifyResult)) {
                log.warn("[SMS] 验证失败 phone={} scene={} verifyResult={}",
                        maskPhone(phone), scene,
                        body.model != null ? body.model.verifyResult : "null");
                throw new IllegalArgumentException("验证码错误或已过期，请重新获取");
            }

            log.info("[SMS] 验证通过 phone={} scene={}", maskPhone(phone), scene);
            return Boolean.TRUE;

        } catch (SmsVerifyException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SMS] 验证异常 phone={} scene={}", maskPhone(phone), scene, e);
            throw new SmsVerifyException("验证码校验异常，请重试");
        }
    }

    // 错误码映射
    private String mapSendErrorCode(String errCode) {
        if (errCode == null) return "短信发送失败，请稍后重试";
        return switch (errCode) {
            case "MOBILE_NUMBER_ILLEGAL"  -> "手机号格式不正确";
            case "BUSINESS_LIMIT_CONTROL" -> "今日发送次数已达上限，请明日再试";
            case "FREQUENCY_FAIL"         -> "发送过于频繁，请稍后再试";
            case "INVALID_PARAMETERS"     -> "请求参数异常，请联系客服";
            case "FUNCTION_NOT_OPENED"    -> "短信服务未开通，请联系客服";
            default -> "短信发送失败，请稍后重试";
        };
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
