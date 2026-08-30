package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.PO.Attraction;
import org.dachuang_team.dc_backend_services.domain.PO.HotelHomestay;
import org.dachuang_team.dc_backend_services.services.IAttractionService;
import org.dachuang_team.dc_backend_services.repository.HotelHomestayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AttractionController {

    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private HotelHomestayRepository hotelHomestayRepository;

    @GetMapping("/attractions/all")
    public Result<List<Attraction>> getAllAttractions() {
        try {
            List<Attraction> attractions = attractionService.getAllAttractions();
            return Result.success("获取景点成功", attractions);
        } catch (Exception e) {
            return Result.error(500, "获取景点失败: " + e.getMessage());
        }
    }

    @GetMapping("/hotels/all")
    public Result<List<HotelHomestay>> getAllHotels() {
        try {
            List<HotelHomestay> hotels = hotelHomestayRepository.findAll();
            return Result.success("获取酒店成功", hotels);
        } catch (Exception e) {
            return Result.error(500, "获取酒店失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除景点/酒店
     * @param id 景点/酒店ID
     * @return 删除结果
     */
    @DeleteMapping("/attractions/delete")
    public Result<Void> deleteAttraction(Long id) {
        try {
            boolean deleted = attractionService.deleteAttractionById(id);
            if (deleted) {
                return Result.success("删除成功", null);
            } else {
                return Result.error(404, "数据不存在");
            }
        } catch (Exception e) {
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }

    /**
     * 添加景点/酒店
     * @param attraction 景点/酒店对象
     * @return 添加结果
     */
    @PostMapping("/attractions/add")
    public Result<Attraction> addAttraction(@RequestBody Attraction attraction) {
        try {
            Attraction addedAttraction = attractionService.addAttraction(attraction);
            if (addedAttraction != null) {
                return Result.success("添加成功", addedAttraction);
            } else {
                return Result.error(500, "添加失败");
            }
        } catch (Exception e) {
            return Result.error(500, "添加失败: " + e.getMessage());
        }
    }

    /**
     * 修改景点/酒店
     * @param attraction 景点/酒店对象
     * @return 修改结果
     */
    @PutMapping("/attractions/update")
    public Result<Attraction> updateAttraction(@RequestBody Attraction attraction) {
        try {
            Attraction updatedAttraction = attractionService.updateAttraction(attraction);
            if (updatedAttraction != null) {
                return Result.success("修改成功", updatedAttraction);
            } else {
                return Result.error(404, "数据不存在");
            }
        } catch (Exception e) {
            return Result.error(500, "修改失败: " + e.getMessage());
        }
    }
}
