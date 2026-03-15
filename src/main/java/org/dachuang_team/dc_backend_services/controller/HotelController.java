package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.hotelHomestay;
import org.dachuang_team.dc_backend_services.repository.hotelHomestayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class HotelController {
    @Autowired
    private hotelHomestayRepository hotelHomestayRepository;

    @PostMapping("/hotels/add")
    public Result<hotelHomestay> addHotel(@RequestBody hotelHomestay hotel) {
        try {
            if (hotel.getHotelName() == null || hotel.getHotelName().isBlank()) {
                return Result.error(400, "酒店名字不能为空");
            }
            hotel.setLastUpdated(LocalDateTime.now());
            hotelHomestay saved = hotelHomestayRepository.save(hotel);
            return Result.success("添加成功", saved);
        } catch (Exception e) {
            return Result.error(500, "添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/hotels/update")
    public Result<hotelHomestay> updateHotel(@RequestBody hotelHomestay hotel) {
        try {
            if (hotel.getHotelId() == null) {
                return Result.error(400, "缺少ID");
            }
            Optional<hotelHomestay> existing = hotelHomestayRepository.findById(hotel.getHotelId());
            if (existing.isEmpty()) {
                return Result.error(404, "数据不存在");
            }
            hotel.setLastUpdated(LocalDateTime.now());
            hotelHomestay saved = hotelHomestayRepository.save(hotel);
            return Result.success("修改成功", saved);
        } catch (Exception e) {
            return Result.error(500, "修改失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/hotels/delete")
    public Result<Void> deleteHotel(@RequestParam Long id) {
        try {
            if (!hotelHomestayRepository.existsById(id)) {
                return Result.error(404, "数据不存在");
            }
            hotelHomestayRepository.deleteById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
}
