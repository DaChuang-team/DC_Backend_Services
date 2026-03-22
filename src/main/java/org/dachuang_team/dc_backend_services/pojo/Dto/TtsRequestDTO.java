package org.dachuang_team.dc_backend_services.pojo.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TtsRequestDTO {
    @JsonProperty(required = true)
    private String text;            // tts要合成的文本
    private String voiceType;       // 音色方案

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoiceType() {
        return voiceType;
    }

    public void setVoiceType(String voiceType) {
        this.voiceType = voiceType;
    }
}
