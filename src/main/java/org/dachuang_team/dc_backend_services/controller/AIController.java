package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.pojo.Dto.AIInteractionDTO;
import org.dachuang_team.dc_backend_services.services.AIService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private UserService userService;

    @PostMapping("/plan")
    public ResponseEntity<AIInteractionDTO.RuralTravelPlan> getPlan(
            @RequestBody AIInteractionDTO.UserPlanRequest request) {

        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // 验证用户是否已认证
            if (currentUserId == null) {
                return ResponseEntity.status(401).build(); // 未认证
            }

            // 验证请求内容是否有效
            if (request.content() == null || request.content().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // 验证模型版本是否有效
            if (request.modelVersion() < 0 || request.modelVersion() > 1) { // 目前仅支持 0（豆包1.6）和 1（豆包1.8），后续可根据实际情况增加版本
                return ResponseEntity.badRequest().body(null); // modelVersion 无效
            }

            String modelVersionInfo = switch (request.modelVersion()) {
                case 0 -> "Doubao-Seed-1.6 251015";
                case 1 -> "Doubao-Seed-1.8 251015";
                default -> "Unknown Model Version";
            };

            int modelPrice = switch (request.modelVersion()) {
                case 0 -> 3; // 豆包1.6积分费用
                case 1 -> 5; // 豆包1.8积分费用
                default -> 0; // 默认价格
            };

            // 调用 Service 时传入 request 中的 content
            AIInteractionDTO.RuralTravelPlan plan = aiService.generateTravelPlan(request.content(), request.modelVersion());
            AIInteractionDTO.RuralTravelPlan updatedPlan = new AIInteractionDTO.RuralTravelPlan(
                    plan.routeTheme(),
                    plan.experienceValue(),
                    plan.steps(),
                    plan.finalCultureSummary(),
                    modelVersionInfo
            );
            // 扣减用户积分
            userService.deductPoints(currentUserId, modelPrice); // 扣减 3 积分
            return ResponseEntity.ok(updatedPlan);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
