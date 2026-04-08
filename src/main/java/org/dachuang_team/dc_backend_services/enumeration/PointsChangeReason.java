package org.dachuang_team.dc_backend_services.enumeration;

import org.dachuang_team.dc_backend_services.services.TtsService;

public enum PointsChangeReason {
    CHECK_IN,
    AI_ROUTINE_GENERATION,
    AI_CONVERSATION,
    AI_IMAGE_RECOGNITION,
    AD_REWARD,
    SYSTEM_CORRECTION,
    TTS_USAGE;

    public static boolean isValidReason(String reason) {
        for (PointsChangeReason r : PointsChangeReason.values()) {
            if (r.name().equals(reason)) {
                return true;
            }
        }
        return false;
    }
}
