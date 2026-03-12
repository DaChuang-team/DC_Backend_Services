package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Dispatcher;
import okhttp3.ConnectionPool;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.model.completion.chat.ResponseFormatJSONSchemaJSONSchemaParam;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.PreDestroy;
import org.dachuang_team.dc_backend_services.pojo.Dto.AIInteractionDTO;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AIService implements IAIServices{

    private final ArkService arkService;
    private final ObjectMapper mapper;

    @Value("${volcengine.ark.endpoint-id}")
    private String endpointId;

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
    public AIInteractionDTO.RuralTravelPlan generateTravelPlan(String query) {
        try {
            //定义消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content("你是一位专业的乡村旅游规划师。请根据用户的提示词提供游玩路线和文化体验规划。不要推荐具体的酒店、餐厅或商品")
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
                          "explanation": { "type": "string" },
                          "output": { "type": "string" }
                        },
                        "required": ["dayIndex", "explanation", "output"]
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
                return mapper.readValue(content, AIInteractionDTO.RuralTravelPlan.class);
            }

            throw new RuntimeException("AI 未返回有效内容");

        } catch (Exception e) {
            throw new RuntimeException("行程规划生成失败: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        if (arkService != null) {
            arkService.shutdownExecutor();
        }
    }
}
