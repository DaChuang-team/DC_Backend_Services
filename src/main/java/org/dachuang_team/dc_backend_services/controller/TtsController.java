package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.TtsRequestDTO;
import org.dachuang_team.dc_backend_services.services.TtsService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.dachuang_team.dc_backend_services.enumeration.TtsVoiceType.isValidVoiceType;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    @Autowired
    TtsService ttsService;

    @Autowired
    UserService userService;

    @PostMapping("/generate")
    public Result<String> generateTts(@RequestBody TtsRequestDTO request) {

        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (request.getText() == null || request.getText().isBlank()) {
            return Result.error(400, "请求参数错误: 生成文本不能为空，请重新输入", null);
        }

        if (request.getText().length() > 500) {
            return Result.error(400, "请求参数错误: 文本长度不能超过500", null);
        }

        // 验证音色方案，如果未提供或无效，则使用默认值
        String validatedVoiceType = (request.getVoiceType() == null || request.getVoiceType().isBlank())
                ? "zh_female_vv_uranus_bigtts"
                : request.getVoiceType();

        if (!isValidVoiceType(validatedVoiceType)) {
            return Result.error(400, "请求参数错误: 无效的声音方案", null);
        }


        try {
            String base64Audio = ttsService.synthesizeTextToBase64(request.getText(), validatedVoiceType);

            // 扣减用户积分
            userService.deductPoints(currentUserId, 5, "TTS_USAGE");

            return Result.success("TTS生成成功", base64Audio);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "请求参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }

}
