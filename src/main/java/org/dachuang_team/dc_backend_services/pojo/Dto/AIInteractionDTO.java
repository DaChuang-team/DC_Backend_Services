package org.dachuang_team.dc_backend_services.pojo.Dto;
import java.util.List;

public class AIInteractionDTO {
    /**
     * 对应 Schema 中的顶级对象
     */
    public record RuralTravelPlan(
            String routeTheme,
            String experienceValue,
            List<ActivityStep> steps, // 对应 Schema 中的 steps 数组
            String finalCultureSummary // 对应 Schema 中的 finalAnswer 位置
    ) {}

    /**
     * 对应 Schema 中的 Step 定义
     */
    public record ActivityStep(
            String explanation, // 游玩逻辑说明
            String output       // 具体活动内容
    ) {}

    public record UserPlanRequest(
            String content  // 测试阶段仅包含请求内容
            // String username, // 预留
            // String password
    ) {}
}
