package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.dachuang_team.dc_backend_services.domain.DTO.GaoDeApiDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 高德地图服务实现类
 */
@Service
public class GaoDeService implements IGaoDeService {

    private static final Logger logger = LoggerFactory.getLogger(GaoDeService.class);

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;

    public GaoDeService(
            @Value("${gaode.api.key}") String apiKey,
            @Value("${gaode.api.base-url}") String baseUrl,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(5, 5, java.util.concurrent.TimeUnit.MINUTES))
                .build();
    }

    @Override
    public GaoDeApiDTO.GeocodeResponse geocode(String address, String city) {
        try {
            // 构建请求 URL
            String url = String.format("%s/geocode/geo?address=%s&key=%s",
                    baseUrl,
                    URLEncoder.encode(address, StandardCharsets.UTF_8),
                    apiKey);

            if (city != null && !city.isEmpty()) {
                url += "&city=" + URLEncoder.encode(city, StandardCharsets.UTF_8);
            }

            // 发起请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {  //检查 HTTP 响应是否成功（状态码 200-299）如果失败，抛出异常
                    throw new IOException("地理编码请求失败：" + response.code());
                }

                String responseBody = response.body().string();  //读取响应体内容
                JsonNode rootNode = objectMapper.readTree(responseBody);  //将响应体内容解析为 JSON 树结构

                // 解析响应
                if (rootNode.has("geocodes") && rootNode.get("geocodes").isArray()  //检查响应体是否包含 geocodes 数组
                        && rootNode.get("geocodes").size() > 0) {
                    JsonNode geocode = rootNode.get("geocodes").get(0); //获取地址的地理编码信息，即第一个元素
                    
                    String location = geocode.has("location") ? geocode.get("location").asText() : "";  //获取 location 字段值
                    String[] parts = location.split(",");  //将 location 字段值按逗号分隔.高德返回的格式是 "经度,纬度" ，需要拆分
                    double longitude = parts.length > 0 ? Double.parseDouble(parts[0]) : 0.0;  //将经度部分转换为 double 类型
                    double latitude = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0;  //将纬度部分转换为 double 类型

                    //返回地址的地理编码信息，包括地址、经度、纬度、格式化地址等
                    return new GaoDeApiDTO.GeocodeResponse(
                            address,
                            location,
                            longitude,
                            latitude,
                            geocode.has("formatted_address") ? geocode.get("formatted_address").asText() : ""
                    );
                }

                throw new RuntimeException("未找到地址的地理编码");
            }
        } catch (Exception e) {
            logger.error("地理编码失败：{}", e.getMessage(), e);
            throw new RuntimeException("地理编码失败：" + e.getMessage(), e);
        }
    }

    @Override
    public GaoDeApiDTO.ReverseGeocodeResponse reverseGeocode(double longitude, double latitude, String extensions) {
        try {
            // 构建请求 URL
            String url = String.format("%s/geocode/regeo?location=%s&key=%s",
                    baseUrl,
                    longitude + "," + latitude,
                    apiKey);

            if (extensions != null && !extensions.isEmpty()) {
                url += "&extensions=" + extensions;
            }

            // 发起请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("逆地理编码请求失败：" + response.code());
                }

                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // 解析响应
                if (rootNode.has("regeocode")) {
                    JsonNode regeocode = rootNode.get("regeocode");
                    JsonNode addressComponent = regeocode.has("addressComponent") ? 
                            regeocode.get("addressComponent") : objectMapper.createObjectNode();

                    // 解析地址组件为结构化对象
                    GaoDeApiDTO.AddressComponent addressComponentObj = new GaoDeApiDTO.AddressComponent(
                            addressComponent.has("province") ? addressComponent.get("province").asText() : "",
                            addressComponent.has("city") ? addressComponent.get("city").asText() : "",
                            addressComponent.has("citycode") ? addressComponent.get("citycode").asText() : "",
                            addressComponent.has("district") ? addressComponent.get("district").asText() : "",
                            addressComponent.has("towncode") ? addressComponent.get("towncode").asText() : "",
                            addressComponent.has("township") ? addressComponent.get("township").asText() : "",
                            addressComponent.has("neighborhood") ? addressComponent.get("neighborhood").asText() : "",
                            addressComponent.has("building") ? addressComponent.get("building").asText() : "",
                            addressComponent.has("streetNumber") ? addressComponent.get("streetNumber").asText() : "",
                            addressComponent.has("street") ? addressComponent.get("street").asText() : ""
                    );

                    return new GaoDeApiDTO.ReverseGeocodeResponse(
                            regeocode.has("formatted_address") ? regeocode.get("formatted_address").asText() : "",
                            addressComponentObj,
                            addressComponent.has("city") ? addressComponent.get("city").asText() : "",
                            addressComponent.has("district") ? addressComponent.get("district").asText() : "",
                            addressComponent.has("province") ? addressComponent.get("province").asText() : "",
                            addressComponent.has("streetNumber") ? addressComponent.get("streetNumber").asText() : "",
                            regeocode.has("neighborhood") ? regeocode.get("neighborhood").asText() : "",
                            regeocode.has("poi") ? regeocode.get("poi").asText() : "",
                            regeocode.has("poitype") ? regeocode.get("poitype").asText() : ""
                    );
                }

                throw new RuntimeException("逆地理编码失败");
            }
        } catch (Exception e) {
            logger.error("逆地理编码失败：{}", e.getMessage(), e);
            throw new RuntimeException("逆地理编码失败：" + e.getMessage(), e);
        }
    }

    @Override
    public GaoDeApiDTO.RoutePlanningResponse routePlanning(
            double originLongitude, double originLatitude,
            double destinationLongitude, double destinationLatitude,
            String type, String strategy) {
        try {
            // 构建请求 URL
            String url = String.format("%s/direction/%s?origin=%s&destination=%s&key=%s",
                    baseUrl,
                    type != null ? type : "driving",
                    originLongitude + "," + originLatitude,
                    destinationLongitude + "," + destinationLatitude,
                    apiKey);

            if (strategy != null && !strategy.isEmpty()) {
                url += "&strategy=" + strategy;
            }

            // 发起请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("路径规划请求失败：" + response.code());
                }

                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // 解析响应
                if (rootNode.has("route") && rootNode.get("route").has("paths")
                        && rootNode.get("route").get("paths").isArray()
                        && rootNode.get("route").get("paths").size() > 0) {
                    
                    JsonNode path = rootNode.get("route").get("paths").get(0);
                    String distance = path.has("distance") ? path.get("distance").asText() : "0";
                    String duration = path.has("duration") ? path.get("duration").asText() : "0";
                    
                    List<GaoDeApiDTO.RouteStep> steps = new ArrayList<>();
                    if (path.has("steps") && path.get("steps").isArray()) {
                        for (JsonNode stepNode : path.get("steps")) {
                            String stepDistance = stepNode.has("distance") ? stepNode.get("distance").asText() : "0";
                            String stepDuration = stepNode.has("duration") ? stepNode.get("duration").asText() : "0";
                            
                            steps.add(new GaoDeApiDTO.RouteStep(
                                    stepNode.has("instruction") ? stepNode.get("instruction").asText() : "",
                                    stepDistance,
                                    stepDuration,
                                    formatDistance(stepDistance),
                                    formatDuration(stepDuration),
                                    stepNode.has("start_location") ? stepNode.get("start_location").asText() : "",
                                    stepNode.has("end_location") ? stepNode.get("end_location").asText() : "",
                                    stepNode.has("action") ? stepNode.get("action").asText() : "",
                                    stepNode.has("assistant_action") ? stepNode.get("assistant_action").asText() : ""
                            ));
                        }
                    }

                    return new GaoDeApiDTO.RoutePlanningResponse(
                            distance,
                            duration,
                            formatDistance(distance),
                            formatDuration(duration),
                            originLongitude + "," + originLatitude,
                            destinationLongitude + "," + destinationLatitude,
                            steps
                    );
                }

                throw new RuntimeException("路径规划失败");
            }
        } catch (Exception e) {
            logger.error("路径规划失败：{}", e.getMessage(), e);
            throw new RuntimeException("路径规划失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<GaoDeApiDTO.POISearchResponse> poiSearch(String keywords, String city, String type, int page, int pageSize) {
        try {
            // 构建请求 URL
            String url = String.format("%s/place/text?keywords=%s&key=%s",
                    baseUrl,
                    URLEncoder.encode(keywords, StandardCharsets.UTF_8),
                    apiKey);

            if (city != null && !city.isEmpty()) {
                url += "&city=" + URLEncoder.encode(city, StandardCharsets.UTF_8);
            }

            if (type != null && !type.isEmpty()) {
                url += "&types=" + URLEncoder.encode(type, StandardCharsets.UTF_8);
            }

            url += "&page=" + page;
            url += "&offset=" + pageSize;

            return executePOISearch(url);
        } catch (Exception e) {
            logger.error("POI 搜索失败：{}", e.getMessage(), e);
            throw new RuntimeException("POI 搜索失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<GaoDeApiDTO.POISearchResponse> poiAroundSearch(
            String keywords, double longitude, double latitude,
            String type, int radius, int page, int pageSize) {
        try {
            // 构建请求 URL
            String url = String.format("%s/place/around?keywords=%s&location=%s&key=%s",
                    baseUrl,
                    URLEncoder.encode(keywords, StandardCharsets.UTF_8),
                    longitude + "," + latitude,
                    apiKey);

            if (type != null && !type.isEmpty()) {
                url += "&types=" + URLEncoder.encode(type, StandardCharsets.UTF_8);
            }

            url += "&radius=" + Math.min(radius, 5000); // 最大 5000 米
            url += "&page=" + page;
            url += "&offset=" + pageSize;

            return executePOISearch(url);
        } catch (Exception e) {
            logger.error("周边 POI 搜索失败：{}", e.getMessage(), e);
            throw new RuntimeException("周边 POI 搜索失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行 POI 搜索请求
     */
    private List<GaoDeApiDTO.POISearchResponse> executePOISearch(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("POI 搜索请求失败：" + response.code());
            }

            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            List<GaoDeApiDTO.POISearchResponse> results = new ArrayList<>();
            
            if (rootNode.has("pois") && rootNode.get("pois").isArray()) {
                for (JsonNode poiNode : rootNode.get("pois")) {
                    String location = poiNode.has("location") ? poiNode.get("location").asText() : "";
                    String[] parts = location.split(",");
                    double longitude = parts.length > 0 ? Double.parseDouble(parts[0]) : 0.0;
                    double latitude = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0;

                    results.add(new GaoDeApiDTO.POISearchResponse(
                            poiNode.has("id") ? poiNode.get("id").asText() : "",
                            poiNode.has("name") ? poiNode.get("name").asText() : "",
                            poiNode.has("type") ? poiNode.get("type").asText() : "",
                            poiNode.has("typecode") ? poiNode.get("typecode").asText() : "",
                            poiNode.has("address") ? poiNode.get("address").asText() : "",
                            location,
                            longitude,
                            latitude,
                            poiNode.has("tel") ? poiNode.get("tel").asText() : "",
                            poiNode.has("distance") ? poiNode.get("distance").asText() : "",
                            poiNode.has("rating") ? poiNode.get("rating").asText() : "",
                            poiNode.has("biz_ext") ? poiNode.get("biz_ext").asText() : "",
                            poiNode.has("timestamp") ? poiNode.get("timestamp").asText() : ""
                    ));
                }
            }

            return results;
        }
    }

    //已弃置暂时保留
    @Override
    public GaoDeApiDTO.IPLocationResponse ipLocation(String ip, String type) {
        try {
            // 构建请求 URL
            StringBuilder urlBuilder = new StringBuilder(baseUrl + "/ip?key=" + apiKey);
            
            // 如果传入了 IP 参数，才添加到 URL 中（没传一定不要加，否则会返回空结果数组）
            if (ip != null && !ip.isEmpty()) {
                urlBuilder.append("&ip=").append(URLEncoder.encode(ip, StandardCharsets.UTF_8));
            }
            
            if (type != null && !type.isEmpty()) {
                urlBuilder.append("&type=").append(type);
            }
            
            String url = urlBuilder.toString();
            logger.debug("IP 定位请求 URL：{}", url);

            // 发起请求
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("IP 定位请求失败：" + response.code());
                }

                String responseBody = response.body().string();
                System.out.println("========== IP 定位 API 原始响应 ==========");
                System.out.println(responseBody);
                System.out.println("==========================================");
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // 检查 API 返回状态
                String status = rootNode.has("status") ? rootNode.get("status").asText() : "0";
                String info = rootNode.has("info") ? rootNode.get("info").asText() : "";
                String infocode = rootNode.has("infocode") ? rootNode.get("infocode").asText() : "";
                
                logger.info("IP 定位 API 响应 - status: {}, info: {}, infocode: {}", status, info, infocode);
                
                if (!"1".equals(status)) {
                    logger.error("IP 定位 API 返回失败 - status: {}, info: {}, infocode: {}", status, info, infocode);
                    throw new RuntimeException("IP 定位失败：" + info);
                }

                // 解析响应 - 注意：高德 IP 定位返回的可能是空数组 [] 或字符串
                JsonNode provinceNode = rootNode.get("province");
                JsonNode cityNode = rootNode.get("city");
                
                logger.debug("provinceNode: {} (isNull: {}, isArray: {}, isTextual: {})", 
                    provinceNode, 
                    provinceNode == null, 
                    provinceNode != null && provinceNode.isArray(),
                    provinceNode != null && provinceNode.isTextual());
                logger.debug("cityNode: {} (isNull: {}, isArray: {}, isTextual: {})", 
                    cityNode, 
                    cityNode == null, 
                    cityNode != null && cityNode.isArray(),
                    cityNode != null && cityNode.isTextual());
                
                // 如果是数组且为空，说明 API Key 没有 IP 定位权限或该 IP 无法定位
                if ((provinceNode.isArray() && provinceNode.isEmpty()) || 
                    (cityNode.isArray() && cityNode.isEmpty())) {
                    // 检查是否是局域网 IP
                    String provinceText = provinceNode.isTextual() ? provinceNode.asText() : "";
                    if ("局域网".equals(provinceText)) {
                        logger.warn("IP 定位返回局域网，该 IP 为内网地址，无法定位");
                    } else {
                        logger.warn("IP 定位返回空结果，可能 API Key 未开通 IP 定位服务或该 IP 无法定位");
                    }
                    return new GaoDeApiDTO.IPLocationResponse(
                            ip != null && !ip.isEmpty() ? ip : "当前请求 IP",
                            "",
                            0.0,
                            0.0,
                            provinceText,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                    );
                }

                // 正常解析
                String province = provinceNode.isTextual() ? provinceNode.asText() : "";
                String city = cityNode.isTextual() ? cityNode.asText() : "";
                
                if (!province.isEmpty() || !city.isEmpty()) {
                    // 高德 IP 定位返回的是 rectangle（矩形区域），格式为 "左下角坐标;右上角坐标"
                    JsonNode rectangleNode = rootNode.get("rectangle");
                    String rectangle = rectangleNode.isTextual() ? rectangleNode.asText() : "";
                    double longitude = 0.0;
                    double latitude = 0.0;
                    String location = "";
                    
                    if (!rectangle.isEmpty() && rectangle.contains(";")) {
                        // 取左下角坐标作为中心点
                        String[] rectParts = rectangle.split(";");
                        if (rectParts.length > 0) {
                            String[] coords = rectParts[0].split(",");
                            if (coords.length >= 2) {
                                longitude = Double.parseDouble(coords[0]);
                                latitude = Double.parseDouble(coords[1]);
                                location = longitude + "," + latitude;
                            }
                        }
                    }

                    return new GaoDeApiDTO.IPLocationResponse(
                            ip != null && !ip.isEmpty() ? ip : "当前请求 IP",
                            location,
                            longitude,
                            latitude,
                            province,
                            city,
                            rootNode.has("adcode") ? rootNode.get("adcode").asText() : "",
                            rootNode.has("district") ? rootNode.get("district").asText() : "",
                            rootNode.has("isp") ? rootNode.get("isp").asText() : "",
                            rootNode.has("areacode") ? rootNode.get("areacode").asText() : "",
                            rootNode.has("country") ? rootNode.get("country").asText() : "",
                            rootNode.has("citycode") ? rootNode.get("citycode").asText() : ""
                    );
                }

                throw new RuntimeException("IP 定位失败：返回数据格式异常");
            }
        } catch (Exception e) {
            logger.error("IP 定位失败：{}", e.getMessage(), e);
            throw new RuntimeException("IP 定位失败：" + e.getMessage(), e);
        }
    }

    /**
     * 格式化距离显示
     * @param distanceMeters 距离（米）
     * @return 格式化后的距离字符串
     */
    private String formatDistance(String distanceMeters) {
        try {
            int meters = Integer.parseInt(distanceMeters);
            if (meters >= 1000) {
                return String.format("%.2f 公里", meters / 1000.0);
            }
            return meters + " 米";
        } catch (Exception e) {
            return distanceMeters + " 米";
        }
    }

    /**
     * 格式化时长显示
     * @param durationSeconds 时长（秒）
     * @return 格式化后的时长字符串
     */
    private String formatDuration(String durationSeconds) {
        try {
            int seconds = Integer.parseInt(durationSeconds);
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            
            if (hours > 0) {
                return String.format("%d 小时%d分钟", hours, minutes);
            } else if (minutes > 0) {
                return String.format("%d分钟", minutes);
            }
            return seconds + "秒";
        } catch (Exception e) {
            return durationSeconds + "秒";
        }
    }
}
