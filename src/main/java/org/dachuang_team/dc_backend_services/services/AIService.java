package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volcengine.ark.runtime.model.completion.chat.*;
import com.volcengine.ark.runtime.model.responses.common.ResponsesCaching;
import com.volcengine.ark.runtime.model.responses.common.ResponsesThinking;
import com.volcengine.ark.runtime.model.responses.constant.ResponsesConstants;
import com.volcengine.ark.runtime.model.responses.content.InputContentItemImage;
import com.volcengine.ark.runtime.model.responses.item.ItemEasyMessage;
import com.volcengine.ark.runtime.model.responses.item.MessageContent;
import com.volcengine.ark.runtime.model.responses.request.CreateResponsesRequest;
import com.volcengine.ark.runtime.model.responses.request.ResponsesInput;
import com.volcengine.ark.runtime.model.responses.response.ResponseObject;
import com.volcengine.ark.runtime.model.responses.item.ItemOutputMessage;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItem;
import com.volcengine.ark.runtime.model.responses.content.OutputContentItemText;

import jakarta.transaction.Transactional;
import okhttp3.Dispatcher;
import okhttp3.ConnectionPool;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.PreDestroy;
import org.dachuang_team.dc_backend_services.pojo.AISessionContext;
import org.dachuang_team.dc_backend_services.pojo.Dto.AIImgInteractionDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.AITextInteractionDTO;
import org.dachuang_team.dc_backend_services.repository.AISessionContextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AIService implements IAIServices{

    private final ArkService arkService;
    private final ObjectMapper mapper;

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    @Autowired
    private AISessionContextRepository contextRepository;

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

            String endpointId = getEndPointId(modelVersion);

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
    @Deprecated
    // 此接口仅供初始版本测试使用，目前已弃用。后续请使用recognizeImage方法，该方法支持上下文缓存和会话管理，更适合实际应用场景
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
                    .text("你是一名经验丰富、专业且热情的现场旅游讲解员，正在用真实、自然的口语为游客讲解。\n" +
                            "用户当前定位：" + userLocation + "\n" +
                            "用户刚刚上传了一张图片，你首先准确识别图片中最主要的主体（只关注最突出、最核心的那一个），然后结合用户的地理位置，撰写一篇完整、流畅、自然的口语讲解文案。\n\n" +

                            "讲解文案必须像真人导游在现场说话一样，连续、自然地说下去，不要出现任何编号或结构化格式。\n" +
                            "它应该是一段连贯的讲话，主语不能省略，句子之间要自然衔接，使用口语化表达，比如‘您现在看到的这座……’、‘咱们来聊聊它的历史……’、‘特别有意思的是……’等，让人听起来舒服、亲切。\n\n" +

                            "讲解内容一定要自然覆盖以下几个方面（但要融成一整段话，不要分点）：\n" +
                            "- 先介绍主体的名字、确切位置、是什么类型（建筑/景点/展品/画作等），并结合图片里能看到的细节描述它的当前样子。\n" +
                            "- 再详细讲它的历史沿革：从起源到现在的完整故事，包括建造或创作的背景、关键年代、重要历史事件、功能变迁、经历过的战争或修复等（信息必须准确，如有争议请说‘据主流史料记载’）。\n" +
                            "- 接着讲它的文化和艺术价值：风格特点、主要设计师或艺术家、象征意义、在当地和世界上的地位。\n\n" +

                            "额外的用户需求：" + query + "\n" +
                            "如果此项不为空，请优先自然融入上面的讲解中（可调整顺序或增加针对性内容）；若为空则忽略。\n\n" +

                            "讲解风格与TTS要求（非常重要）：\n" +
                            "- 用第一人称，像正在现场陪用户讲解一样亲切、自然、热情。\n" +
                            "- 句子长度适中（每句15-25字左右），节奏感好，便于语音合成朗读。\n" +
                            "- 避免书面语、列表、缩写、主语省略；多用连接词让前后自然过渡。\n" +
                            "- 整体长度控制在200-300字左右（既完整又有故事感，听起来不累）。\n" +
                            "- 最后用一句温暖的结束语收尾。\n\n" +

                            "重要：如果主体实在无法清晰辨认，请在 recognizedResult 字段写 '无法清晰识别'，并在 explanation 中礼貌说明原因并请求用户重新上传图片或提供更多描述。"
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
                                "recognizedResult": { "type": "string" },
                                "explanation": { "type": "string" }
                              },
                              "required": ["recognizedResult", "explanation"]
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
            String endpointId = getEndPointId(modelVersion);

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

    // 支持上下文缓存的图像识别接口，供前端新开启一个图像解析会话时调用，后续用户在同一会话中追加对话时可以使用返回的responseId进行上下文关联
    @Transactional
    @Override
    public AIImgInteractionDTO.ImageRecognitionResponse recognizeImage(Long userId, AIImgInteractionDTO.ImageRecognitionRequest requestDTO) {
        try {
            // 获取模型对应的EndpointId
            String endpointId = getEndPointId(requestDTO.modelVersion());

            // 构建 ResponsesInput（多模态：先图片，后文本 prompt）
            ResponsesInput.Builder inputBuilder = ResponsesInput.builder();

            // 图片
            inputBuilder.addListItem(ItemEasyMessage.builder()
                    .role(ResponsesConstants.MESSAGE_ROLE_USER)
                    .content(MessageContent.builder()
                            .addListItem(InputContentItemImage.builder()
                                    .imageUrl(requestDTO.imgUrl())
                                    .build())
                            .build())
                    .build());

            String query = requestDTO.content() != null ? requestDTO.content() : "";

            // 完整的文本prompt
            String fullPrompt = String.format(
                            "你是一名经验丰富、专业且热情的现场私人旅游讲解员，正在用真实、自然的口语为游客讲解。\n" +
                            "用户当前定位：" + requestDTO.userLocation() + "\n" +
                            "用户刚刚上传了一张图片，你首先准确识别图片中最主要的主体（只关注最突出、最核心的那一个），然后结合用户的地理位置，撰写一篇完整、流畅、自然的口语讲解文案。\n\n" +

                            "讲解文案必须像真人导游在现场说话一样，连续、自然地说下去，不要出现任何编号或结构化格式。\n" +
                            "它应该是一段连贯的讲话，主语不能省略，句子之间要自然衔接，使用口语化表达，比如‘您现在看到的这座……’、‘咱们来聊聊它的历史……’、‘特别有意思的是……’等，让人听起来舒服、亲切。\n\n" +

                            "讲解内容一定要至少自然覆盖以下几个方面（但要融成一整段话，不要分点）：\n" +
                            "- 先介绍主体的名字、确切位置、是什么类型（建筑/景点/展品/画作等），并结合图片里能看到的细节描述它的当前样子。\n" +
                            "- 再详细讲它的历史沿革：从起源到现在的完整故事，包括建造或创作的背景、关键年代、重要历史事件、功能变迁、经历过的战争或修复等（信息必须准确，如有争议请说‘据主流史料记载’）。\n" +
                            "- 接着讲它的文化和艺术价值：风格特点、主要设计师或艺术家、象征意义、在当地和世界上的地位。\n\n" +

                            "额外的用户需求：" + query + "\n" +
                            "如果此项不为空，请优先自然融入上面的讲解中（可调整顺序或增加针对性内容）；若为空则忽略。\n\n" +

                            "讲解风格与TTS要求（非常重要）：\n" +
                            "- 用第一人称，像正在现场陪用户讲解一样亲切、自然、热情。\n" +
                            "- 句子长度适中（每句15-25字左右），节奏感好，便于语音合成朗读。\n" +
                            "- 避免书面语、列表、缩写、主语省略；多用连接词让前后自然过渡。\n" +
                            "- 整体长度控制在200-300字左右（既完整又有故事感，听起来不累）。\n" +
                            "- 最后用一句温暖的结束语收尾。\n\n" +

                            "重要：如果主体实在无法清晰辨认，请直接在讲解文案中说明原因并请求用户重新上传图片或提供更多描述。"
            );

            inputBuilder.addListItem(ItemEasyMessage.builder()
                    .role(ResponsesConstants.MESSAGE_ROLE_USER)
                    .content(MessageContent.builder()
                            .stringValue(fullPrompt)
                            .build())
                    .build());

            // 当前时间戳 + 3天（259200秒），单位为秒。加100秒确保数据库中的过期时间比AI平台的稍晚，避免边界问题
            long expireAt = Instant.now().getEpochSecond() + 259200 + 100;

            // 构造最终请求
            CreateResponsesRequest request = CreateResponsesRequest.builder()
                    .model(endpointId)
                    .input(inputBuilder.build())
                    .caching(ResponsesCaching.builder().type("enabled").build())
                    .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())
                    .expireAt(expireAt)
                    .store(true)
                    .build();

            // 发起请求
            ResponseObject resp = arkService.createResponse(request);

            // 解析
            String explanation = extractExplanationFromResponse(resp);

            // 更新/持久化会话上下文
            String sessionId = createSessionContext(userId, resp.getId(), endpointId);

            // 返回结果对象
            return new AIImgInteractionDTO.ImageRecognitionResponse(
                    explanation,
                    getModelVersionInfo(requestDTO.modelVersion()),
                    resp.getId(),
                    sessionId,
                    LocalDateTime
                            .now()
                            .withSecond(0)
                            .withNano(0)
                            .plusDays(3)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            );

        } catch (Exception e) {
            throw new RuntimeException("AI 图像识别服务异常: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AIImgInteractionDTO.FollowUpResponse continueConversation(Long userId, AIImgInteractionDTO.FollowUpRequest requestDTO) {
        try {
            // 查找会话上下文
            AISessionContext context = contextRepository.findByUserIdAndSessionId(userId, requestDTO.sessionID());

            // 检查上下文是否存在且未过期（超过3天未交互则过期）
            if(context == null || context.getExpireTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("会话不存在或已过期，请重新上传图片发起新对话");
            }


            // 构建请求，关联上下文
            CreateResponsesRequest followUpRequest = CreateResponsesRequest.builder()
                    .model(context.getModelEndpoint())
                    .input(ResponsesInput.builder()
                            .addListItem(ItemEasyMessage.builder()
                                    .role(ResponsesConstants.MESSAGE_ROLE_USER)
                                    .content(MessageContent.builder()
                                            .stringValue(requestDTO.content())
                                            .build())
                                    .build())
                            .build())
                    .previousResponseId(context.getLastResponseId()) // 关联上下文的关键
                    .caching(ResponsesCaching.builder().type("enabled").build())
                    .thinking(ResponsesThinking.builder().type(ResponsesConstants.THINKING_TYPE_DISABLED).build())
                    .store(true)
                    .build();

            var response = arkService.createResponse(followUpRequest);

            String explanation = extractExplanationFromResponse(response);

            // 更新会话上下文中的 lastResponseId 和 lastResponseTime
            updateSessionContext(userId, requestDTO.sessionID(), response.getId());

            return new AIImgInteractionDTO.FollowUpResponse(
                    explanation,
                    response.getId(),
                    LocalDateTime
                            .now()
                            .withSecond(0)
                            .withNano(0)
                            .plusDays(3)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            );

        } catch (Exception e) {
            throw new RuntimeException("继续对话失败: " + e.getMessage(), e);
        }
    }

    // 根据模型版本选择对应的EndpointId
    private String getEndPointId(int modelVersion) {
        return switch (modelVersion) {
            case 0 -> "ep-20260202151315-zvslq"; // 豆包1.6版本
            case 1 -> "ep-20260312135710-f8kfz"; // 豆包1.8版本
            default -> throw new IllegalArgumentException("未知的模型版本: " + modelVersion);
        };
    }

    private String getModelVersionInfo(int modelVersion) {
        return switch (modelVersion) {
            case 0 -> "Doubao-Seed-1.6 251015";
            case 1 -> "Doubao-Seed-1.8 251228";
            default -> "UNKNOWN_MODEL";
        };
    }

    // 图片识别接口调用成功后，持久化会话上下文，供后续追加对话使用
    private String createSessionContext(Long userId, String responseId, String endpointId) {
        AISessionContext context = new AISessionContext();

        // 生成唯一的会话ID，返回给前端，后续追问必须带上它
        String newSessionId = generateUUID();

        context.setUserId(userId);
        context.setSessionId(newSessionId);
        context.setLastResponseId(responseId); // 存储第一轮识别的 ID
        context.setModelEndpoint(endpointId);
        context.setLastResponseTime(LocalDateTime.now());
        context.setExpireTime(LocalDateTime.now().plusDays(3)); // 对应会话的过期时间，超过3天未交互则过期

        contextRepository.save(context);
        return newSessionId; // 返回给 Service 层，最终返回给前端
    }

    // 追加对话时调用，更新会话上下文中的lastResponseId和lastResponseTime
    private void updateSessionContext(Long userId, String sessionId, String newResponseId) {
        // 根据userId和sessionId查找对应的会话上下文
        AISessionContext context = contextRepository.findByUserIdAndSessionId(userId, sessionId);

        if(context == null) {
            throw new RuntimeException("会话不存在或已过期，请开启新对话");
        }

        // 再次检查是否过期
        if (context.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("会话已超过 3 天有效期，请开启新对话");
        }

        // 更新链条指针
        context.setLastResponseId(newResponseId);
        context.setLastResponseTime(LocalDateTime.now());
        context.setExpireTime(LocalDateTime.now().plusDays(3));

        contextRepository.save(context);
    }

    // 生成唯一标识会话的UUID
    public String generateUUID() {
        return java.util.UUID.randomUUID().toString();
    }

    private String extractExplanationFromResponse(ResponseObject resp) {
        if (resp == null || resp.getOutput() == null || resp.getOutput().isEmpty()) {
            log.warn("ResponseObject 输出为空或不存在");
            return "";
        }

        StringBuilder fullText = new StringBuilder();

        // output是List<ItemOutputMessage>
        for (Object itemObj : resp.getOutput()) {
            // 强转为SDK提供的 ItemOutputMessage 类型
            if (itemObj instanceof ItemOutputMessage) {
                ItemOutputMessage messageItem = (ItemOutputMessage) itemObj;

                // 获取content列表
                List<OutputContentItem> contentItems = messageItem.getContent();
                if (contentItems == null || contentItems.isEmpty()) {
                    continue;
                }

                // 遍历content，取出所有 output_text的text
                for (OutputContentItem contentItem : contentItems) {
                    if (contentItem instanceof OutputContentItemText) {
                        OutputContentItemText textItem = (OutputContentItemText) contentItem;
                        if ("output_text".equals(textItem.getType()) && textItem.getText() != null) {
                            fullText.append(textItem.getText());
                        }
                    }
                }
            } else {
                log.warn("output 中的项不是 ItemOutputMessage 类型: {}",
                        itemObj != null ? itemObj.getClass().getName() : "null");
            }
        }

        String result = fullText.toString().trim();

        if (result.isEmpty()) {
            log.warn("最终提取到的文本为空，原始 output: {}", resp.getOutput());
        }

        return result;
    }
}
