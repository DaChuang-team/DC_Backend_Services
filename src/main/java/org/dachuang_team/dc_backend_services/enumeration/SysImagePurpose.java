package org.dachuang_team.dc_backend_services.enumeration;


public enum SysImagePurpose {
    MAIN_PAGE_BANNER,
    PRODUCT_PAGE_BANNER,
    USER_DEFAULT_AVATAR,
    MERCHANT_DEFAULT_BANNER;

    public static boolean isValidPurpose(String purpose) {
        for (SysImagePurpose p : SysImagePurpose.values()) {
            if (p.name().equals(purpose)) {
                return true;
            }
        }
        return false;
    }
}
