package org.dachuang_team.dc_backend_services.domain.DTO;

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
            String responseId,        // 当前回合 ID，前端可保存用于后续追问
            String sessionID,         // 会话ID，前端需保存用于唯一标识一段对话
            String expireTime         // 该对话的过期时间，超过该时间后 sessionID 将失效，前端需保存并显示给用户以提示对话有效期
    ) {}


    // 后续追加对话请求 (纯文本)
    public record FollowUpRequest(
            String content,             // 用户后续追加的文本内容
            String sessionID            // 会话ID，必须与当前对话一致，用于关联上下文
    ) {}

    // 后续追加对话响应 (纯文本)
    public record FollowUpResponse(
            String text,              // AI 回复的纯文本内容
            String responseId,        // 本轮回复的 ID，用于下一轮追问的 previousResponseId
            String newExpireTime      // 更新后的过期时间，前端需更新保存显示给用户
    ) {}
}