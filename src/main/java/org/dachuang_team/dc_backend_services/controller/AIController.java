package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.pojo.Dto.AIImgInteractionDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.AITextInteractionDTO;
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
    @GetMapping("/plan")
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
                        .body(Result.error(400, "无效的模型版本"));
            }

            String modelVersionInfo = switch (request.modelVersion()) {
                case 0 -> "Doubao-Seed-1.6 251015";
                case 1 -> "Doubao-Seed-1.8 251228";
                default -> "Unknown Model Version";
            };

            int modelPrice = switch (request.modelVersion()) {
                case 0 -> 3;
                case 1 -> 5;
                default -> 0;
            };

            // 调用AI服务生成旅行计划
            AITextInteractionDTO.RuralTravelPlan plan = aiService.generateTravelPlan(request.content(), request.modelVersion());
            AITextInteractionDTO.RuralTravelPlan updatedPlan = new AITextInteractionDTO.RuralTravelPlan(
                    plan.routeTheme(),
                    plan.experienceValue(),
                    plan.steps(),
                    plan.finalCultureSummary(),
                    modelVersionInfo
            );

            // 扣减用户积分
            userService.deductPoints(currentUserId, modelPrice, "AI_INTERACTION");
            return ResponseEntity.ok(Result.success("操作成功", updatedPlan));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }

    //上传图片结合用户位置生成景点讲解接口
    @GetMapping("/ImgRecognition")
    public ResponseEntity<Result<AIImgInteractionDTO.ImageRecognitionResponse>> getImgRecognition(
            @RequestBody AIImgInteractionDTO.ImageRecognitionRequest request) {

        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 验证用户是否已认证
            if (currentUserId == null) {
                return ResponseEntity.status(401)
                        .body(Result.error(401, "未认证"));
            }

            // 验证请求是否包含图片URL和位置信息
            if (request.userLocation() == null || request.userLocation().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "位置信息不能为空"));
            }
            if (request.imgUrl() == null || request.imgUrl().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "图片URL不能为空"));
            }

            String fixedContent = request.content()==null || request.content().isBlank() ? "无" : request.content();

            // 验证模型版本是否有效
            if (request.modelVersion() < 0 || request.modelVersion() > 1) {
                return ResponseEntity.badRequest()
                        .body(Result.error(400, "无效的模型版本"));
            }

            String modelVersionInfo = switch (request.modelVersion()) {
                case 0 -> "Doubao-Seed-1.6 251015";
                case 1 -> "Doubao-Seed-1.8 251228";
                default -> "Unknown Model Version";
            };

            int modelPrice = switch (request.modelVersion()) { //图片识别模型价格高于文本模型
                case 0 -> 6;
                case 1 -> 10;
                default -> 0;
            };

            AIImgInteractionDTO.ImageRecognitionResponse response = aiService.getImageRecognition(
                    fixedContent,
                    request.modelVersion(),
                    request.imgUrl(),
                    request.userLocation()
            );
            AIImgInteractionDTO.ImageRecognitionResponse updatedResponse = new AIImgInteractionDTO.ImageRecognitionResponse(
                    response.recognizedContent(),
                    response.explanation(),
                    modelVersionInfo
            );


            userService.deductPoints(currentUserId, modelPrice, "AI_INTERACTION");
            return ResponseEntity.ok(Result.success("操作成功", updatedResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Result.error(500, "服务器内部错误"));
        }
    }
}
