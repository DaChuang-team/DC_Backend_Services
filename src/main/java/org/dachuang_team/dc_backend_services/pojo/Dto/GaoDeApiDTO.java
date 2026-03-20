package org.dachuang_team.dc_backend_services.pojo.Dto;

import java.util.List;

/**
 * 高德地图 API 交互 DTO
 */
public class GaoDeApiDTO {

    /**
     * 地理编码请求（地址转坐标）
     */
    public record GeocodeRequest(
            String address,      // 要解析的地址
            String city          // 城市名称（可选）
    ) {}

    /**
     * 地理编码响应
     */
    public record GeocodeResponse(
            String address,      // 原始地址
            String location,     // 坐标：经度，纬度
            double longitude,    // 经度
            double latitude,     // 纬度
            String formattedAddress  // 格式化后的地址
    ) {}

    /**
     * 逆地理编码请求（坐标转地址）
     */
    public record ReverseGeocodeRequest(
            double longitude,    // 经度
            double latitude,     // 纬度
            String extensions    // 是否返回详细信息：base（默认）/all
    ) {}

    /**
     * 逆地理编码响应
     */
    public record ReverseGeocodeResponse(
            String formattedAddress,  // 格式化地址
            String addressComponent,  // 地址组成部分
            String city,              // 城市
            String district,          // 区县
            String province,          // 省份
            String streetNumber,      // 街道
            String neighborhood       // 周边信息
    ) {}

    /**
     * 路径规划请求
     */
    public record RoutePlanningRequest(
            double originLongitude,   // 起点经度
            double originLatitude,    // 起点纬度
            double destinationLongitude,  // 终点经度
            double destinationLatitude,   // 终点纬度
            String strategy,          // 规划策略：0-速度优先，1-费用优先，2-距离优先，3-舒适优先
            String type               // 路径类型：driving（驾车）/walking（步行）/transit（公交）
    ) {}

    /**
     * 路径规划响应
     */
    public record RoutePlanningResponse(
            String distance,         // 距离（米）
            String duration,         // 耗时（秒）
            String startLocation,    // 起点坐标
            String endLocation,      // 终点坐标
            List<RouteStep> steps    // 路径步骤
    ) {}

    /**
     * 路径步骤
     */
    public record RouteStep(
            String instruction,      // 行走指令
            String distance,         // 本段距离（米）
            String duration,         // 本段耗时（秒）
            String startLocation,    // 起点坐标
            String endLocation       // 终点坐标
    ) {}

    /**
     * POI 搜索请求
     */
    public record POISearchRequest(
            String keywords,         // 搜索关键词
            String city,             // 城市名称
            double location,         // 中心点坐标：经度，纬度
            String type,             // POI 类型
            int radius,              // 搜索半径（米），最大 5000
            int page,                // 页码
            int pageSize             // 每页数量
    ) {}

    /**
     * POI 搜索响应
     */
    public record POISearchResponse(
            String poiId,            // POI ID
            String name,             // 名称
            String type,             // 类型
            String address,          // 地址
            String location,         // 坐标
            double longitude,        // 经度
            double latitude,         // 纬度
            String tel,              // 电话
            String distance          // 距离中心点的距离（米）
    ) {}

    /**
     * 周边搜索请求
     */
    public record POIAroundSearchRequest(
            String keywords,         // 搜索关键词
            double location,         // 中心点坐标：经度，纬度
            String type,             // POI 类型
            int radius,              // 搜索半径（米），最大 5000
            int page,                // 页码
            int pageSize             // 每页数量
    ) {}

    /**
     * IP 定位请求
     */
    public record IPLocationRequest(
            String ip,               // IP 地址（可选，不传则定位当前请求 IP）
            String type              // 定位类型：4（IPv4）/ 11（IPv6）
    ) {}

    /**
     * IP 定位响应
     */
    public record IPLocationResponse(
            String ip,               // IP 地址
            String location,         // 坐标：经度，纬度
            double longitude,        // 经度
            double latitude,         // 纬度
            String province,         // 省份
            String city,             // 城市
            String adcode,           // 城市编码
            String district,         // 区县
            String isp,              // 运营商
            String areaCode          // 区域代码
    ) {}
}
