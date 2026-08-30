package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.TtsRequestDTO;
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

        if (request.getText().length() > 1000) {
            return Result.error(400, "请求参数错误: 文本长度不能超过1000", null);
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

            int pointsCost = getPointsCost(request.getText().length());

            // 扣减用户积分
            userService.deductPoints(currentUserId, pointsCost, "TTS_USAGE");

            return Result.success("TTS生成成功", base64Audio);
        } catch (IllegalArgumentException e) {
            return Result.error(400, "请求参数错误: " + e.getMessage(), null);
        } catch (Exception e) {
            return Result.error(500, "服务器错误: " + e.getMessage(), null);
        }
    }


    // 根据文本长度计算积分扣减，规则如下：0-50字符扣除1积分，51-100字符扣除2积分，超过100字符每增加200字符扣除2积分
    public int getPointsCost(int textLength) {
        if (textLength <= 0) {
            return 0;
        }else if (textLength <= 50) {
            return 1;
        } else if (textLength <= 100) {
            return 2;
        } else {
            int extraLength = textLength - 100;
            int extraBlocks = (int) Math.ceil(extraLength / 200.0);
            return 2 + extraBlocks * 2;
        }
    }

}
