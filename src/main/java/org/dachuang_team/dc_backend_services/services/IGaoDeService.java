package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.GaoDeApiDTO;

import java.util.List;

/**
 * 高德地图服务接口
 * 向高德地图api发送请求，获取地图服务结果
 */
public interface IGaoDeService {
    
    /**
     * 地理编码（地址转坐标）
     * @param address 地址
     * @param city 城市名称（可选）
     * @return 地理编码结果
     */
    GaoDeApiDTO.GeocodeResponse geocode(String address, String city);
    
    /**
     * 逆地理编码（坐标转地址）
     * @param longitude 经度
     * @param latitude 纬度
     * @param extensions 是否返回详细信息
     * @return 逆地理编码结果
     */
    GaoDeApiDTO.ReverseGeocodeResponse reverseGeocode(double longitude, double latitude, String extensions);
    
    /**
     * 路径规划
     * @param originLongitude 起点经度
     * @param originLatitude 起点纬度
     * @param destinationLongitude 终点经度
     * @param destinationLatitude 终点纬度
     * @param type 路径类型：driving/walking/transit
     * @param strategy 规划策略
     * @return 路径规划结果
     */
    GaoDeApiDTO.RoutePlanningResponse routePlanning(
            double originLongitude, double originLatitude,
            double destinationLongitude, double destinationLatitude,
            String type, String strategy);
    
    /**
     * POI 搜索
     * @param keywords 关键词
     * @param city 城市
     * @param type POI 类型
     * @param page 页码
     * @param pageSize 每页数量
     * @return POI 列表
     */
    List<GaoDeApiDTO.POISearchResponse> poiSearch(String keywords, String city, String type, int page, int pageSize);
    
    /**
     * 周边 POI 搜索
     * @param keywords 关键词
     * @param longitude 中心点经度
     * @param latitude 中心点纬度
     * @param type POI 类型
     * @param radius 搜索半径
     * @param page 页码
     * @param pageSize 每页数量
     * @return POI 列表
     */
    List<GaoDeApiDTO.POISearchResponse> poiAroundSearch(
            String keywords, double longitude, double latitude, 
            String type, int radius, int page, int pageSize);
    
    /**
     * IP 定位
     * @param ip IP 地址（可选，不传则定位当前请求 IP）
     * @param type 定位类型：4（IPv4）/ 11（IPv6）
     * @return IP 定位结果
     */
    GaoDeApiDTO.IPLocationResponse ipLocation(String ip, String type);
}
