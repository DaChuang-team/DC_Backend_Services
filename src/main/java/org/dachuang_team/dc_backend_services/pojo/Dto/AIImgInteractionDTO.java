package org.dachuang_team.dc_backend_services.pojo.Dto;


public class AIImgInteractionDTO {
    public record ImageRecognitionRequest(
            String userLocation, // 用户位置信息
            String imgUrl,
            String content, // 仅包含请求内容
            int modelVersion
    ) {}

    public record ImageRecognitionResponse(
            String recognizedContent, // 识别出的内容
            String explanation, // 识别结果的解释
            String modelVersion
    ) {}
}
