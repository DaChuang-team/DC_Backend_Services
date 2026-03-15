package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Attraction;
import org.dachuang_team.dc_backend_services.repository.AttractionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttractionService implements IAttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Override
    public List<Attraction> getAllAttractions() {
        // 返回所有标签不为 "酒店" 的记录，即景点
        return attractionRepository.findByAttractionTagNot("酒店");
    }

    @Override
    public List<Attraction> getAllHotels() {
        // 返回所有标签为 "酒店" 的记录
        return attractionRepository.findByAttractionTag("酒店");
    }

    @Override
    public boolean deleteAttractionById(Long attractionId) {
        try {
            // 检查记录是否存在
            if (attractionRepository.existsById(attractionId)) {
                // 调用repository的deleteById方法删除记录
                attractionRepository.deleteById(attractionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            // 捕获异常，返回删除失败
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Attraction addAttraction(Attraction attraction) {
        try {
            // 调用repository的save方法保存记录
            return attractionRepository.save(attraction);
        } catch (Exception e) {
            // 捕获异常，打印堆栈信息
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Attraction updateAttraction(Attraction attraction) {
        try {
            // 检查记录是否存在
            if (attractionRepository.existsById(attraction.getAttractionId())) {
                // 调用repository的save方法更新记录
                return attractionRepository.save(attraction);
            }
            return null;
        } catch (Exception e) {
            // 捕获异常，打印堆栈信息
            e.printStackTrace();
            return null;
        }
    }
}