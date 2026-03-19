package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.AIImgInteractionDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.AITextInteractionDTO;

public interface IAIServices {
    AITextInteractionDTO.RuralTravelPlan generateTravelPlan(String query, int modelVersion);
    AIImgInteractionDTO.ImageRecognitionResponse getImageRecognition(String query, int modelVersion, String imageUrl, String userLocation);

}
