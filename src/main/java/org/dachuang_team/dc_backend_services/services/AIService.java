package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.completion.chat.*;
import okhttp3.Dispatcher;
import okhttp3.ConnectionPool;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.PreDestroy;
import org.dachuang_team.dc_backend_services.pojo.Dto.AIImgInteractionDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.AITextInteractionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AIService implements IAIServices{

    private final ArkService arkService;
    private final ObjectMapper mapper;

    public AIService(@Value("${volcengine.ark.api-key}") String apiKey, ObjectMapper mapper) {
        this.arkService = ArkService.builder()
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .dispatcher(new Dispatcher())
                .apiKey(apiKey)
                .baseUrl("https://ark.cn-beijing.volces.com/api/v3")
                .build();
        this.mapper = mapper;
    }

    @Override
    public AITextInteractionDTO.RuralTravelPlan generateTravelPlan(String query, int modelVersion) {
        try {
            //定义消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content("你是一位专业的乡村旅游规划师。请根据用户的提示词提供合理的游玩路线和文化体验规划，需要综合考虑地点之间的交通时间、交通便利程度、用户预算等、用户偏好等。不要推荐具体的酒店、餐厅或商品。" +
                            "若用户提出的请求跟乡村旅游规划不相关，请礼貌拒绝并提示用户重新输入与乡村旅游相关的请求。")
                    .build());
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(query)
                    .build());

            //JSON Schema
            String schemaJson = """
                                {
                                  "type": "object",
                                  "properties": {
                                    "routeTheme": { "type": "string" },
                                    "experienceValue": { "type": "string" },
                                    "steps": {
                                      "type": "array",
                                      "items": {
                                        "type": "object",
                                        "properties": {
                                          "dayIndex": { "type": "integer" },
                                          "mainAttractions": {
                                            "type": "array",
                                            "items": {
                                              "type": "object",
                                              "properties": {
                                                "name": { "type": "string" }
                                              },
                                              "required": ["name"]
                                            }
                                          },
                                          "explanation": { "type": "string" },
                                          "output": { "type": "string" }
                                        },
                                        "required": ["dayIndex", "mainAttractions", "explanation", "output"]
                                      }
                                    },
                                    "finalCultureSummary": { "type": "string" }
                                  },
                                  "required": ["routeTheme", "experienceValue", "steps", "finalCultureSummary"]
                                }
                                """;
            JsonNode schemaNode = mapper.readTree(schemaJson);

            //强制响应格式
            ChatCompletionRequest.ChatCompletionRequestResponseFormat responseFormat = new ChatCompletionRequest.ChatCompletionRequestResponseFormat(
                    "json_schema",
                    new ResponseFormatJSONSchemaJSONSchemaParam(
                            "travel_plan",
                            "乡村旅游规划结构化响应",
                            schemaNode,
                            true // 严格模式
                    )
            );

            String endpointId = switch (modelVersion) {
                case 0 -> "ep-20260202151315-zvslq"; //1.6
                case 1 -> "ep-20260312135710-f8kfz"; //1.8
                // 预留接口
                default -> throw new IllegalArgumentException("Unsupported model version: " + modelVersion);
            };

            //发起请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(endpointId)
                    .messages(messages)
                    .responseFormat(responseFormat)
                    .thinking(new ChatCompletionRequest.ChatCompletionRequestThinking("disabled"))
                    .build();

            var response = arkService.createChatCompletion(request);

            //解析结果
            if (!response.getChoices().isEmpty()) {
                String content = (String) response.getChoices().get(0).getMessage().getContent();
                return mapper.readValue(content, AITextInteractionDTO.RuralTravelPlan.class);
            }

            throw new RuntimeException("AI 未返回有效内容");

        } catch (Exception e) {
            throw new RuntimeException("行程规划生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public AIImgInteractionDTO.ImageRecognitionResponse getImageRecognition(String query, int modelVersion, String imageUrl, String userLocation) {
        try {
            // 构造消息内容
            List<ChatCompletionContentPart> contentParts = new ArrayList<>();
            contentParts.add(ChatCompletionContentPart.builder()
                    .type("image_url")
                    .imageUrl(new ChatCompletionContentPart.ChatCompletionContentPartImageURL(imageUrl))
                    .build());
            contentParts.add(ChatCompletionContentPart.builder()
                    .type("text")
                    .text("你是一名经验丰富、专业且热情的现场旅游讲解员。\n" +
                            "用户当前定位：" + userLocation + "\n" +
                            "用户刚刚上传了一张图片，图片主体通常是某一公共建筑、景点、博物馆展品或艺术品。\n\n" +

                            "你的任务是：\n" +
                            "首先准确识别图片中最主要的主体（只关注最突出、最核心的那一个），然后根据这个主体结合用户的地理位置撰写一篇完整但简洁的讲解文案。\n" +

                            "讲解文案必须包含以下结构，按顺序组织：\n" +
                            "1. 主体介绍\n" +
                            "名称、确切位置、类型（建筑/景点/展品/画作等）、结合图片可见细节的当前外观简述。\n\n" +
                            "2. 历史沿革\n" +
                            "从起源到现代的完整脉络：建造/创作/发现时间与背景、关键历史节点、重要事件、功能变迁、战争/损毁/修复经过、保护历程等。信息必须准确，如有争议请写“据主流史料记载”。\n\n" +
                            "3. 文化艺术价值\n" +
                            "风格流派、主要设计师/艺术家、象征意义、在当地/全球的地位与影响。\n\n" +

                            "额外的用户需求：" + query + "\n" +
                            "如果此项不为空，请优先响应用户具体要求（可调整以上结构或增加针对性内容）；若为空则忽略。\n\n" +

                            "讲解风格要求：\n" +
                            "- 生动有趣、专业但不枯燥，字数控制在 200字 \n" +

                            "重要：如果主体实在无法辨认，请在 recognizedContent 写 '无法清晰识别'，并在 explanation 中礼貌说明原因并请求重新上传。\n\n"
                            )
                    .build()
            );

            // 构造消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .multiContent(contentParts)
                    .build());

            // 定义JSON Schema
            String schemaJson = """
                            {
                              "type": "object",
                              "properties": {
                                "recognizedContent": { "type": "string" },
                                "explanation": { "type": "string" }
                              },
                              "required": ["recognizedContent", "explanation"]
                            }
                            """;
            JsonNode schemaNode = mapper.readTree(schemaJson);

            // 强制响应格式
            ChatCompletionRequest.ChatCompletionRequestResponseFormat responseFormat = new ChatCompletionRequest.ChatCompletionRequestResponseFormat(
                    "json_schema",
                    new ResponseFormatJSONSchemaJSONSchemaParam(
                            "image_recognition",
                            "图像识别结构化响应",
                            schemaNode,
                            true
                    )
            );

            // 模型版本映射
            String endpointId = switch (modelVersion) {
                case 0 -> "ep-20260202151315-zvslq"; //1.6
                case 1 -> "ep-20260312135710-f8kfz"; //1.8
                default -> throw new IllegalArgumentException("Unsupported model version: " + modelVersion);
            };

            // 构造请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(endpointId)
                    .messages(messages)
                    .responseFormat(responseFormat)
                    .build();

            // 发起请求
            var response = arkService.createChatCompletion(request);

            // 解析响应
            if (!response.getChoices().isEmpty()) {
                String content = (String) response.getChoices().get(0).getMessage().getContent();
                return mapper.readValue(content, AIImgInteractionDTO.ImageRecognitionResponse.class);
            }

            throw new RuntimeException("AI 未返回有效内容");

        } catch (Exception e) {
            throw new RuntimeException("图像识别失败: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        if (arkService != null) {
            arkService.shutdownExecutor();
        }
    }
}
