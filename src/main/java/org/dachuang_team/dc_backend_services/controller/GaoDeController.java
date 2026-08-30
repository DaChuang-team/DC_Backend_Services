package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.GaoDeApiDTO;
import org.dachuang_team.dc_backend_services.services.IGaoDeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 高德地图 API 控制器
 * 提供地理编码、路径规划、POI 搜索等功能
 */
@RestController
@RequestMapping("/api/gaode")
public class GaoDeController {

    @Autowired
    private IGaoDeService gaoDeService;

    private static final Logger logger = LoggerFactory.getLogger(GaoDeController.class);
    
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
     * @param originLongitude 起点经度（必填，来自前端 GPS 定位）
     * @param originLatitude 起点纬度（必填，来自前端 GPS 定位）
     * @param destinationLongitude 终点经度（必填）
     * @param destinationLatitude 终点纬度（必填）
     * @param type 路径类型（driving/walking/transit）
     * @param strategy 规划策略（0-速度优先，1-费用优先，2-距离优先，3-舒适优先）
     * @return 路径规划结果
     */
    @GetMapping("/route")
    public Result<GaoDeApiDTO.RoutePlanningResponse> routePlanning(
            @RequestParam double originLongitude,
            @RequestParam double originLatitude,
            @RequestParam double destinationLongitude,
            @RequestParam double destinationLatitude,
            @RequestParam(defaultValue = "driving") String type,
            @RequestParam(defaultValue = "0") String strategy) {
        try {
            // 验证起点坐标有效性
            if (!isValidCoordinate(originLongitude, originLatitude)) {
                return Result.error(400, "起点坐标无效，请检查 GPS 定位数据");
            }
            
            // 验证终点坐标有效性
            if (!isValidCoordinate(destinationLongitude, destinationLatitude)) {
                return Result.error(400, "终点坐标无效");
            }

            logger.info("路径规划请求 - 起点：{},{} 终点：{},{} 类型：{}", 
                originLongitude, originLatitude, destinationLongitude, destinationLatitude, type);

            GaoDeApiDTO.RoutePlanningResponse response = gaoDeService.routePlanning(
                    originLongitude, originLatitude, destinationLongitude, destinationLatitude, type, strategy);

            return Result.success("路径规划成功", response);
        } catch (Exception e) {
            logger.error("路径规划失败：{}", e.getMessage(), e);
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
     * @param longitude 中心点经度（必填，如果是用户位置，需要GPS提供经度）
     * @param latitude 中心点纬度（必填，如果是用户位置，需要GPS提供纬度）
     * @param type POI 类型
     * @param radius 搜索半径（米，最大 5000）
     * @param page 页码
     * @param pageSize 每页数量
     * @return POI 列表
     */
    @GetMapping("/poi/around")
    public Result<List<GaoDeApiDTO.POISearchResponse>> poiAroundSearch(
            @RequestParam String keywords,
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1000") int radius,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            if (keywords == null || keywords.isEmpty()) {
                return Result.error(400, "搜索关键词不能为空");
            }

            // 验证坐标有效性
            if (!isValidCoordinate(longitude, latitude)) {
                return Result.error(400, "GPS 坐标无效，请检查定位数据");
            }

            // 限制搜索半径
            if (radius > 5000) {
                radius = 5000;
            }
            if (radius < 100) {
                radius = 100;
            }

            logger.info("周边 POI 搜索 - 关键词：{} 位置：{},{} 半径：{}m", 
                keywords, longitude, latitude, radius);

            List<GaoDeApiDTO.POISearchResponse> results = gaoDeService.poiAroundSearch(
                    keywords, longitude, latitude, type, radius, page, pageSize);
            return Result.success("周边 POI 搜索成功", results);
        } catch (Exception e) {
            logger.error("周边 POI 搜索失败：{}", e.getMessage(), e);
            return Result.error(500, "周边 POI 搜索失败：" + e.getMessage());
        }
    }

    /**
     * IP 定位接口（弃置保留）
     * @param ip IP 地址（可选，不传则定位当前请求 IP）
     * @param type 定位类型：固定为 4（仅支持 IPv4）
     * @return IP 定位结果
     */
    @GetMapping("/ip")
    public Result<GaoDeApiDTO.IPLocationResponse> ipLocation(
            @RequestParam(required = false) String ip,
            @RequestParam(defaultValue = "4") String type) {
        try {
            if (ip != null && !ip.isEmpty() && !isValidIP(ip)) {
                return Result.error(400, "IP 地址格式不正确");
            }

            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            return Result.success("IP 定位成功", response);
        } catch (Exception e) {
            return Result.error(500, "IP 定位失败：" + e.getMessage());
        }
    }

    /**
     * 验证 IP 地址格式是否正确
     * @param ip IP 地址字符串
     * @return 如果格式正确返回 true，否则返回 false
     */
    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // IPv4 地址正则表达式：匹配 0-255 的数字
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        
        // IPv6 地址正则表达式（简化版）
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                            "^:([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
                            "^([0-9a-fA-F]{1,4}:){1,7}:$";
        
        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern);
    }

    /**
     * 验证 GPS 坐标是否有效
     * @param longitude 经度
     * @param latitude 纬度
     * @return 如果坐标有效返回 true，否则返回 false
     */
    private boolean isValidCoordinate(double longitude, double latitude) {
        // 经度范围：-180 到 180
        // 纬度范围：-90 到 90
        return longitude >= -180 && longitude <= 180 && 
               latitude >= -90 && latitude <= 90;
    }
    
}
