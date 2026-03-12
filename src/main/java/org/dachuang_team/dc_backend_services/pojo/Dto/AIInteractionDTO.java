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
            String finalCultureSummary, // 对应 Schema 中的 finalAnswer 位置
            String modelVersion
    ) {}

    /**
     * 对应 Schema 中的 Step 定义
     */
    public record ActivityStep(
            int dayIndex, // 第几天
            String explanation, // 游玩逻辑说明
            String output       // 具体活动内容
    ) {}

    // 模型版本modelVersion暂定0为豆包1.6; 1为豆包1.8,后续可根据实际情况增加
    public record UserPlanRequest(
            int modelVersion,
            String content  // 仅包含请求内容
    ) {}


}
