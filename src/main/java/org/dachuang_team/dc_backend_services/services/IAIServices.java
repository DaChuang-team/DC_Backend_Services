package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.AIImgInteractionDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.AITextInteractionDTO;

/**
 * AI服务接口
 * 提供AI相关的功能，生成旅行计划、图片识别
 */
public interface IAIServices {
    AITextInteractionDTO.RuralTravelPlan generateTravelPlan(String query, int modelVersion, Long userId);
    AIImgInteractionDTO.ImageRecognitionResponse getImageRecognition(String query, int modelVersion, String imageUrl, String userLocation);
    AIImgInteractionDTO.ImageRecognitionResponse recognizeImage(Long userId, AIImgInteractionDTO.ImageRecognitionRequest requestDTO);
    AIImgInteractionDTO.FollowUpResponse continueConversation(Long userId, AIImgInteractionDTO.FollowUpRequest requestDTO);

}
