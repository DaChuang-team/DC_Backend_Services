package org.dachuang_team.dc_backend_services.enumeration;

public enum SmsScene {

    LOGIN(
            "登录验证码方案",
            "100001",
            "{\"code\":\"##code##\",\"min\":\"10\"}",
            600L, 60L, 6L, 1L, 1L
    ),
    REGISTER(
            "注册验证码方案",
            "100001",
            "{\"code\":\"##code##\",\"min\":\"10\"}",
            600L, 60L, 6L, 1L, 1L
    ),
    RESET_PWD(
            "找回密码方案",
            "100003",
            "{\"code\":\"##code##\",\"min\":\"10\"}",
            600L, 60L, 6L, 1L, 1L
    ),
    CHECK_OLD_PHONE(
            "验证绑定手机号方案",
            "100005",
            "{\"code\":\"##code##\",\"min\":\"15\"}",
            900L, 120L, 6L, 1L, 1L
    ),
    CHECK_NEW_PHONE(
            "验证新手机号方案",
            "100004",
            "{\"code\":\"##code##\",\"min\":\"15\"}",
            900L, 120L, 6L, 1L, 1L
    )
    ;

    private final String schemeName;    // 方案名称（如登录、注册等）
    private final String templateCode;  // 短信模板 CODE
    private final String templateParam; // 短信模板参数
    private final Long validTime;       // 过期时间
    private final Long interval;        // 时间间隔
    private final Long codeLength;      // 验证码长度
    private final Long codeType;        // 验证码类型（1=纯数字）
    private final Long duplicatePolicy; // 核验规则（1=重发旧码失效 2=重发旧码有效）

    SmsScene(String schemeName, String templateCode, String templateParam, Long validTime, Long interval, Long codeLength, Long codeType, Long duplicatePolicy) {
        this.schemeName = schemeName;
        this.templateCode = templateCode;
        this.templateParam = templateParam;
        this.validTime = validTime;
        this.interval = interval;
        this.codeLength = codeLength;
        this.codeType = codeType;
        this.duplicatePolicy = duplicatePolicy;
    }

    public Long getDuplicatePolicy() {
        return duplicatePolicy;
    }
    public Long getCodeType() {
        return codeType;
    }
    public Long getCodeLength() {
        return codeLength;
    }
    public Long getInterval() {
        return interval;
    }
    public Long getValidTime() {
        return validTime;
    }
    public String getTemplateParam() {
        return templateParam;
    }
    public String getTemplateCode() {
        return templateCode;
    }
    public String getSchemeName() {
        return schemeName;
    }
}