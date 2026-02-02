package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.AIInteractionDTO;

public interface IAIServices {
    AIInteractionDTO.RuralTravelPlan generateTravelPlan(String query);
}
