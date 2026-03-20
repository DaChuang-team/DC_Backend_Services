package org.dachuang_team.dc_backend_services.pojo.Dto;


public class AIImgInteractionDTO {

    // 首次图像识别请求 (支持图片 + 初始指令)
    public record ImageRecognitionRequest(
            String userLocation, // 用户位置信息
            String imgUrl,
            String content,      // 用户提问或指令
            int modelVersion
    ) {}


    //首次图像识别响应

    public record ImageRecognitionResponse(
            String recognizedResult,  // 识别出的内容
            String modelVersion,      // 使用的模型版本代号
            String responseId         // 当前回合 ID，前端可保存用于后续追问
    ) {}


    // 后续追加对话请求 (纯文本)

    public record FollowUpRequest(
            String content            // 用户后续追加的问题文本
    ) {}

    // 后续追加对话响应 (纯文本)
    public record FollowUpResponse(
            String text,              // AI 回复的纯文本内容
            String responseId,        // 本轮回复的 ID，用于下一轮追问的 previousResponseId
            String modelVersion       // 当前使用的模型版本
    ) {}
}