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
     * 地址组成部分（逆地理编码详细信息）
     */
    public record AddressComponent(
            String province,      // 省份
            String city,          // 城市
            String citycode,      // 城市编码
            String district,      // 区县
            String towncode,      // 乡镇/街道编码
            String township,      // 乡镇/街道
            String neighborhood,  // 社区/小区
            String building,      // 建筑物
            String streetNumber,  // 门牌号
            String street         // 街道
    ) {}

    /**
     * 逆地理编码响应
     */
    public record ReverseGeocodeResponse(
            String formattedAddress,  // 格式化地址
            AddressComponent addressComponent,  // 地址组成部分（结构化数据）
            String city,              // 城市
            String district,          // 区县
            String province,          // 省份
            String streetNumber,      // 街道
            String neighborhood,      // 周边信息
            String poiName,           // 附近 POI 名称
            String poiType            // 附近 POI 类型
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
            String distanceText,     // 格式化距离：如 "1.23 公里"
            String durationText,     // 格式化时长：如 "1 小时 30 分钟"
            String startLocation,    // 起点坐标
            String endLocation,      // 终点坐标
            List<RouteStep> steps    // 路径步骤
    ) {}

    /**
     * 路径步骤
     */
    public record RouteStep(
            String instruction,      // 行走/驾驶指令
            String distance,         // 本段距离（米）
            String duration,         // 本段耗时（秒）
            String distanceText,     // 格式化距离
            String durationText,     // 格式化时长
            String startLocation,    // 起点坐标
            String endLocation,      // 终点坐标
            String action,           // 动作：如"左转"、"右转"、"直行"
            String assistantAction   // 辅助动作：如"靠右行驶"、"进入环岛"
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
            String typecode,         // 类型编码
            String address,          // 地址
            String location,         // 坐标：经度，纬度
            double longitude,        // 经度
            double latitude,         // 纬度
            String tel,              // 电话
            String distance,         // 距离中心点的距离（米）
            String rating,           // 评分
            String bizExt,           // 商业扩展信息
            String timestamp         // 时间戳
    ) {}

    /**
     * POI 搜索结果包装（包含分页信息）
     */
    public record POISearchResult(
            List<POISearchResponse> pois,  // POI 列表
            int totalCount,                // 总记录数
            int currentPage,               // 当前页码
            int pageSize                   // 每页数量
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
            String areaCode,         // 区域代码
            String country,          // 国家
            String cityNameCode      // 城市名称代码
    ) {}
}
