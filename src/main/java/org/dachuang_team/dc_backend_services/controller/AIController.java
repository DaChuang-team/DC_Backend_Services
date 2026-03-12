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

            // 调用 Service 时传入 request 中的 content
            AIInteractionDTO.RuralTravelPlan plan = aiService.generateTravelPlan(request.content());
            AIInteractionDTO.RuralTravelPlan updatedPlan = new AIInteractionDTO.RuralTravelPlan(
                    plan.routeTheme(),
                    plan.experienceValue(),
                    plan.steps(),
                    plan.finalCultureSummary(),
                    "Doubao-Seed-1.6 251015" // 模型版本信息
            );
            // 扣减用户积分
            userService.deductPoints(currentUserId, 3); // 扣减 3 积分
            return ResponseEntity.ok(updatedPlan);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
