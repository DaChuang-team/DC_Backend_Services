package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.GaoDeApiDTO;
import org.dachuang_team.dc_backend_services.services.IGaoDeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地图 API 控制器
 * 提供地理编码、路径规划、POI 搜索等功能
 */
@RestController
@RequestMapping("/api/gaode")
public class GaoDeController {

    @Autowired
    private IGaoDeService gaoDeService;

    /**
     * IP 定位接口
     * @param ip IP 地址（可选，不传则定位当前请求 IP）
     * @param type 定位类型：固定为 4（仅支持 IPv4）
     * @return IP 定位结果
     */
    @GetMapping("/ip")
    public Result<GaoDeApiDTO.IPLocationResponse> ipLocation(
            @RequestParam(required = false) String ip,
            @RequestParam(defaultValue = "4") String type) {
        try {
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            return Result.success("IP 定位成功", response);
        } catch (Exception e) {
            return Result.error(500, "IP 定位失败：" + e.getMessage());
        }
    }

    /**
     * 地理编码接口（地址转坐标）
     * @param request 地理编码请求
     * @return 地理编码结果
     */
    @GetMapping("/geocode")
    public Result<GaoDeApiDTO.GeocodeResponse> geocode(
            @RequestParam String address,
            @RequestParam(required = false) String city) {
        try {
            if (address == null || address.isEmpty()) {
                return Result.error(400, "地址不能为空");
            }

            GaoDeApiDTO.GeocodeResponse response = gaoDeService.geocode(address, city);
            return Result.success("地理编码成功", response);
        } catch (Exception e) {
            return Result.error(500, "地理编码失败：" + e.getMessage());
        }
    }

    /**
     * 逆地理编码接口（坐标转地址）
     * @param longitude 经度
     * @param latitude 纬度
     * @param extensions 是否返回详细信息（base/all）
     * @return 逆地理编码结果
     */
    @GetMapping("/reverseGeocode")
    public Result<GaoDeApiDTO.ReverseGeocodeResponse> reverseGeocode(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "base") String extensions) {
        try {
            GaoDeApiDTO.ReverseGeocodeResponse response = 
                    gaoDeService.reverseGeocode(longitude, latitude, extensions);
            return Result.success("逆地理编码成功", response);
        } catch (Exception e) {
            return Result.error(500, "逆地理编码失败：" + e.getMessage());
        }
    }

    /**
     * 路径规划接口
     * @param origin 起点坐标（格式：经度，纬度），不传则使用当前 IP 定位地址
     * @param destination 终点坐标（格式：经度，纬度）
     * @param type 路径类型（driving/walking/transit）
     * @param strategy 规划策略（0-速度优先，1-费用优先，2-距离优先，3-舒适优先）
     * @return 路径规划结果
     */
    @GetMapping("/route")
    public Result<GaoDeApiDTO.RoutePlanningResponse> routePlanning(
            @RequestParam(required = false) String origin,
            @RequestParam String destination,
            @RequestParam(defaultValue = "driving") String type,
            @RequestParam(defaultValue = "0") String strategy) {
        try {
            double originLon, originLat;
            
            // 如果没有提供起点坐标，使用 IP 定位获取当前位置
            if (origin == null || origin.isEmpty()) {
                GaoDeApiDTO.IPLocationResponse ipLocation;
                try {
                    ipLocation = gaoDeService.ipLocation(null, "4");
                } catch (Exception e) {
                    return Result.error(500, "IP 定位失败：" + e.getMessage());
                }
                originLon = ipLocation.longitude();
                originLat = ipLocation.latitude();
            } else {
                // 解析用户提供的起点坐标
                String[] originParts = origin.split(",");
                if (originParts.length != 2) {
                    return Result.error(400, "起点坐标格式错误，应为：经度，纬度");
                }
                originLon = Double.parseDouble(originParts[0]);
                originLat = Double.parseDouble(originParts[1]);
            }
            
            // 解析终点坐标
            String[] destParts = destination.split(",");
            if (destParts.length != 2) {
                return Result.error(400, "终点坐标格式错误，应为：经度，纬度");
            }
            double destLon = Double.parseDouble(destParts[0]);
            double destLat = Double.parseDouble(destParts[1]);

            GaoDeApiDTO.RoutePlanningResponse response = gaoDeService.routePlanning(
                    originLon, originLat, destLon, destLat, type, strategy);

            return Result.success("路径规划成功", response);
        } catch (Exception e) {
            return Result.error(500, "路径规划失败：" + e.getMessage());
        }
    }

    /**
     * POI 搜索接口
     * @param keywords 搜索关键词
     * @param city 城市名称
     * @param type POI 类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return POI 列表
     */
    @GetMapping("/poi/search")
    public Result<List<GaoDeApiDTO.POISearchResponse>> poiSearch(
            @RequestParam String keywords,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            if (keywords == null || keywords.isEmpty()) {
                return Result.error(400, "搜索关键词不能为空");
            }

            List<GaoDeApiDTO.POISearchResponse> results = 
                    gaoDeService.poiSearch(keywords, city, type, page, pageSize);
            return Result.success("POI 搜索成功", results);
        } catch (Exception e) {
            return Result.error(500, "POI 搜索失败：" + e.getMessage());
        }
    }

    /**
     * 周边 POI 搜索接口
     * @param keywords 搜索关键词
     * @param location 中心点坐标（格式：经度，纬度）
     * @param type POI 类型
     * @param radius 搜索半径（米，最大 5000）
     * @param page 页码
     * @param pageSize 每页数量
     * @return POI 列表
     */
    @GetMapping("/poi/around")
    public Result<List<GaoDeApiDTO.POISearchResponse>> poiAroundSearch(
            @RequestParam String keywords,
            @RequestParam String location,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            if (keywords == null || keywords.isEmpty()) {
                return Result.error(400, "搜索关键词不能为空");
            }

            // 解析坐标
            String[] parts = location.split(",");
            if (parts.length != 2) {
                return Result.error(400, "坐标格式错误，应为：经度，纬度");
            }

            double longitude = Double.parseDouble(parts[0]);
            double latitude = Double.parseDouble(parts[1]);

            List<GaoDeApiDTO.POISearchResponse> results = gaoDeService.poiAroundSearch(
                    keywords, longitude, latitude, type, radius, page, pageSize);
            return Result.success("周边 POI 搜索成功", results);
        } catch (Exception e) {
            return Result.error(500, "周边 POI 搜索失败：" + e.getMessage());
        }
    }
}
