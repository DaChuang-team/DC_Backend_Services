package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Attraction;
import java.util.List;

public interface IAttractionService {
    List<Attraction> getAllAttractions();
    List<Attraction> getAllHotels();
    /**
     * 根据ID删除景点/酒店
     * @param attractionId 景点/酒店ID
     * @return 删除是否成功
     */
    boolean deleteAttractionById(Long attractionId);
    
    /**
     * 添加景点/酒店
     * @param attraction 景点/酒店对象
     * @return 添加后的景点/酒店对象
     */
    Attraction addAttraction(Attraction attraction);
    
    /**
     * 修改景点/酒店
     * @param attraction 景点/酒店对象
     * @return 修改后的景点/酒店对象
     */
    Attraction updateAttraction(Attraction attraction);
}