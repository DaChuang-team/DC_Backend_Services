package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.dachuang_team.dc_backend_services.config.RedisConfig;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TtsService implements ITtsService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${volcengine.tts.app-id}")
    private String appId;

    @Value("${volcengine.tts.access-token}")
    private String accessToken;

    private static final Logger logger = LoggerFactory.getLogger(TtsService.class);

    private final String requestUrl = "https://openspeech.bytedance.com/api/v3/tts/unidirectional";

    @Override
    public String synthesizeTextToBase64(String text, String voiceType) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("输入文本不能为空");
        }

        String reqId = UUID.randomUUID().toString();
        String uid = "backend_services_" + reqId;

        try {
            Map<String, Object> bodyMap = getStringObjectMap(text, uid, voiceType);
            String jsonBody = mapper.writeValueAsString(bodyMap);

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(requestUrl)
                    .post(body)
                    .addHeader("X-Api-App-Id", appId)
                    .addHeader("X-Api-Access-Key", accessToken)
                    .addHeader("X-Api-Resource-Id", "seed-tts-2.0")
                    .addHeader("X-Api-Request-Id", reqId)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    throw new RuntimeException("TTS 请求失败: HTTP " + response.code() + " - " + errBody);
                }

                StringBuilder fullAudioBase64 = new StringBuilder();
                // 使用 Okio 的 BufferedSource 流式读取内容
                okio.BufferedSource source = response.body().source();

                while (!source.exhausted()) {
                    // 读取一行 JSON 字符串 (火山引擎 HTTP Chunked 每一帧通常以换行符分隔)
                    String line = source.readUtf8Line();
                    if (line == null || line.trim().isEmpty()) continue;

                    try {
                        JsonNode json = mapper.readTree(line);
                        int code = json.path("code").asInt(-1);

                        if (code == 0) {
                            // 处理音频数据包
                            String chunkData = json.path("data").asText("");
                            if (!chunkData.isEmpty()) {
                                fullAudioBase64.append(chunkData);
                            }
                        } else if (code == 20000000) {
                            // 处理合成任务结束包
                            logger.info("TTS 任务正常完成 (code=20000000)");
                            break; // 正常退出循环
                        } else {
                            // 处理其他响应 (如文本/时间戳信息) 或 业务错误
                            // 如果 data 为 null 但不是 20000000，可能是文本信息包，这里记录日志即可
                            logger.debug("收到非音频响应数据包: code={}, msg={}", code, json.path("message").asText());
                        }

                    } catch (Exception parseEx) {
                        logger.warn("解析单行 JSON 失败（可能是非标准包）: {}", line);
                    }
                }

                String finalBase64 = fullAudioBase64.toString();
                logger.info("TTS 合成成功: text='{}', 最终 Base64 总长度={}", text, finalBase64.length());

                if (finalBase64.isEmpty()) {
                    throw new RuntimeException("TTS 合成成功但未获取到任何音频片段");
                }

                return finalBase64;
            }

        } catch (Exception e) {
            throw new RuntimeException("TTS 合成异常: " + e.getMessage(), e);
        }
    }

    // request body 定义见 https://www.volcengine.com/docs/6561/1598757?lang=zh#_2-http-chunked%E6%A0%BC%E5%BC%8F%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
    @NotNull
    private static Map<String, Object> getStringObjectMap(String text, String uid, String voice) {
        Map<String, Object> bodyMap = new HashMap<>();

        // user部分
        Map<String, String> userMap = new HashMap<>();
        userMap.put("uid", uid);
        bodyMap.put("user", userMap);

        // req_params部分
        Map<String, Object> reqParamsMap = new HashMap<>();
        reqParamsMap.put("text", text);
        reqParamsMap.put("speaker", voice);

        // audio_params
        Map<String, Object> audioParamsMap = new HashMap<>();
        audioParamsMap.put("format", "mp3");
        audioParamsMap.put("sample_rate", 24000);
        reqParamsMap.put("audio_params", audioParamsMap);

        bodyMap.put("req_params", reqParamsMap);
        return bodyMap;
    }
}
