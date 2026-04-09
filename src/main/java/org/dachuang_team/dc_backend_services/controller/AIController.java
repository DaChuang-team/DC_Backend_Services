package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.domain.DTO.AIImgInteractionDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.AITextInteractionDTO;
import org.dachuang_team.dc_backend_services.services.AIService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.dachuang_team.dc_backend_services.common.Result;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private UserService userService;

    //生成旅行计划接口
    @PostMapping("/plan")
    public ResponseEntity<Result<AITextInteractionDTO.RuralTravelPlan>> getPlan(
            @RequestBody AITextInteractionDTO.UserPlanRequest request) {

        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 验证用户是否已认证
            if (currentUserId == null) {
                return ResponseEntity.status(401)
                        .body(Result.error(401, "未认证"));
            }

            // 验证请求内容是否有效
            if (request.content() == null || request.content().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "请求内容不能为空"));
            }

            // 验证模型版本是否有效
            if (request.modelVersion() < 0 || request.modelVersion() > 1) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "Unsupported model version" + request.modelVersion()));
            }

            String modelVersionInfo = switch (request.modelVersion()) {
                case 0 -> "Doubao-Seed-1.6 251015";
                case 1 -> "Doubao-Seed-1.8 251228";
                default -> "UNKNOWN_MODEL";
            };

            int modelPrice = switch (request.modelVersion()) {
                case 0 -> 3;
                case 1 -> 5;
                default -> 0;
            };

            // 调用AI服务生成旅行计划
            AITextInteractionDTO.RuralTravelPlan plan = aiService.generateTravelPlan(request.content(), request.modelVersion(), currentUserId);
            AITextInteractionDTO.RuralTravelPlan updatedPlan = new AITextInteractionDTO.RuralTravelPlan(
                    plan.routeTheme(),
                    plan.experienceValue(),
                    plan.steps(),
                    plan.finalCultureSummary(),
                    modelVersionInfo
            );

            // 扣减用户积分
            userService.deductPoints(currentUserId, modelPrice, "AI_ROUTINE_GENERATION");
            return ResponseEntity.ok(Result.success("操作成功", updatedPlan));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    //上传图片结合用户位置生成景点讲解接口
    @PostMapping("/ImgRecognition")
    public ResponseEntity<Result<AIImgInteractionDTO.ImageRecognitionResponse>> getImgRecognition(
            @RequestBody AIImgInteractionDTO.ImageRecognitionRequest request) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (currentUserId == null) {
                return ResponseEntity.status(401)
                        .body(Result.error(401, "未认证"));
            }

            // 验证模型版本是否有效
            if (request.modelVersion() < 0 || request.modelVersion() > 1) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "不支持的模型代号: " + request.modelVersion()));
            }

            int modelPrice = switch (request.modelVersion()) {
                case 0 -> 6;
                case 1 -> 10;
                default -> 0;
            };

            AIImgInteractionDTO.ImageRecognitionResponse response = aiService.recognizeImage(currentUserId, request);
            // 扣减用户积分
            userService.deductPoints(currentUserId, modelPrice, "AI_IMAGE_RECOGNITION");
            return ResponseEntity.ok(Result.success("操作成功", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    // 后续追加对话接口，用户在首次图像识别后可以继续追问，提供纯文本问题，返回纯文本回答
    // 支持多轮追问，前端通过传递 sessionID 来确定会话并关联上下文 ，后端通过sessionID查询上次聊天最新的responseId传入AI模型，保证上下文连续
    @PostMapping("/continue-conv")
    public ResponseEntity<Result<AIImgInteractionDTO.FollowUpResponse>> continueConv(
            @RequestBody AIImgInteractionDTO.FollowUpRequest request){
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (currentUserId == null) {
                return ResponseEntity.status(401)
                        .body(Result.error(401, "未认证"));
            }

            if (request.content() == null || request.content().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "请求内容不能为空"));
            }

            if(request.sessionID() == null || request.sessionID().isBlank()){
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "会话ID不能为空"));
            }

            AIImgInteractionDTO.FollowUpResponse response = aiService.continueConversation(currentUserId, request);

            // 追问价格固定为2分/条消息
            userService.deductPoints(currentUserId, 2, "AI_CONVERSATION");
            return ResponseEntity.ok(Result.success("操作成功", response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Result.error(500, "服务器内部错误"));
        }

    }

}
