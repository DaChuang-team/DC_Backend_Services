package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.enumeration.SmsScene;

public interface ISmsCodeService {

    // 发送验证码
    String sendCode(String phone, SmsScene scene);

    // 验证验证码
    Boolean verifyCode(String phone, String code, SmsScene scene);
}
