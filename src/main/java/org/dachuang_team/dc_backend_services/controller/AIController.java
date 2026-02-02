package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.pojo.Dto.AIInteractionDTO;
import org.dachuang_team.dc_backend_services.services.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/plan")
    public ResponseEntity<AIInteractionDTO.RuralTravelPlan> getPlan(
            @RequestBody AIInteractionDTO.UserPlanRequest request) {

        try {
            // 校验请求内容是否为空
            if (request.content() == null || request.content().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // 调用 Service 时传入 request 中的 content
            AIInteractionDTO.RuralTravelPlan plan = aiService.generateTravelPlan(request.content());
            return ResponseEntity.ok(plan);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
