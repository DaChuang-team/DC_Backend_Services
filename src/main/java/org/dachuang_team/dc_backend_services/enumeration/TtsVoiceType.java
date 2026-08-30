package org.dachuang_team.dc_backend_services.enumeration;

public enum TtsVoiceType {
    zh_female_vv_uranus_bigtts, // vivi 2.0
    zh_female_peiqi_uranus_bigtts, // 佩奇猪 2.0
    zh_female_kefunvsheng_uranus_bigtts, // 暖阳女声 2.0
    saturn_zh_male_shuanglangshaonian_tob, // 爽朗少年
    zh_male_shaonianzixin_uranus_bigtts; // 少年梓辛/Brayan 2.0


    public static boolean isValidVoiceType(String voiceType) {
        for (TtsVoiceType v : TtsVoiceType.values()) {
            if (v.name().equals(voiceType)) {
                return true;
            }
        }
        return false;
    }
}
