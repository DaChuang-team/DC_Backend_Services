package org.dachuang_team.dc_backend_services.enumeration;

public enum TtsVoiceType {
    zh_female_vv_uranus_bigtts; // vivi 2.0

    public static boolean isValidVoiceType(String voiceType) {
        for (TtsVoiceType v : TtsVoiceType.values()) {
            if (v.name().equals(voiceType)) {
                return true;
            }
        }
        return false;
    }
}
