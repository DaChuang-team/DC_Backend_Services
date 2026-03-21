package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.dachuang_team.dc_backend_services.pojo.Dto.GaoDeApiDTO;
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
                    JsonNode geocode = rootNode.get("geocodes").get(0);
                    
                    String location = geocode.has("location") ? geocode.get("location").asText() : "";  //获取 location 字段值
                    String[] parts = location.split(",");  //将 location 字段值按逗号分隔.高德返回的格式是 "经度,纬度" ，需要拆分
                    double longitude = parts.length > 0 ? Double.parseDouble(parts[0]) : 0.0;  //将经度部分转换为 double 类型
                    double latitude = parts.length > 1 ? Double.parseDouble(parts[1]) : 0.0;  //将纬度部分转换为 double 类型

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

                    return new GaoDeApiDTO.ReverseGeocodeResponse(
                            regeocode.has("formatted_address") ? regeocode.get("formatted_address").asText() : "",
                            addressComponent.toString(),
                            addressComponent.has("city") ? addressComponent.get("city").asText() : "",
                            addressComponent.has("district") ? addressComponent.get("district").asText() : "",
                            addressComponent.has("province") ? addressComponent.get("province").asText() : "",
                            addressComponent.has("streetNumber") ? addressComponent.get("streetNumber").asText() : "",
                            regeocode.has("neighborhood") ? regeocode.get("neighborhood").asText() : ""
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
                            steps.add(new GaoDeApiDTO.RouteStep(
                                    stepNode.has("instruction") ? stepNode.get("instruction").asText() : "",
                                    stepNode.has("distance") ? stepNode.get("distance").asText() : "0",
                                    stepNode.has("duration") ? stepNode.get("duration").asText() : "0",
                                    stepNode.has("start_location") ? stepNode.get("start_location").asText() : "",
                                    stepNode.has("end_location") ? stepNode.get("end_location").asText() : ""
                            ));
                        }
                    }

                    return new GaoDeApiDTO.RoutePlanningResponse(
                            distance,
                            duration,
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
                            poiNode.has("address") ? poiNode.get("address").asText() : "",
                            location,
                            longitude,
                            latitude,
                            poiNode.has("tel") ? poiNode.get("tel").asText() : "",
                            poiNode.has("distance") ? poiNode.get("distance").asText() : ""
                    ));
                }
            }

            return results;
        }
    }

    @Override
    public GaoDeApiDTO.IPLocationResponse ipLocation(String ip, String type) {
        try {
            // 构建请求 URL
            String url = String.format("%s/ip?ip=%s&key=%s",
                    baseUrl,
                    ip != null && !ip.isEmpty() ? URLEncoder.encode(ip, StandardCharsets.UTF_8) : "",
                    apiKey);

            if (type != null && !type.isEmpty()) {
                url += "&type=" + type;
            }

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
                JsonNode rootNode = objectMapper.readTree(responseBody);

                // 解析响应
                if (rootNode.has("province") || rootNode.has("city")) {
                    String location = rootNode.has("location") ? rootNode.get("location").asText() : "";
                    String[] parts = location.split(",");
                    double longitude = parts.length > 0 && !parts[0].isEmpty() ? Double.parseDouble(parts[0]) : 0.0;
                    double latitude = parts.length > 1 && !parts[1].isEmpty() ? Double.parseDouble(parts[1]) : 0.0;

                    return new GaoDeApiDTO.IPLocationResponse(
                            ip != null && !ip.isEmpty() ? ip : "当前请求 IP",
                            location,
                            longitude,
                            latitude,
                            rootNode.has("province") ? rootNode.get("province").asText() : "",
                            rootNode.has("city") ? rootNode.get("city").asText() : "",
                            rootNode.has("adcode") ? rootNode.get("adcode").asText() : "",
                            rootNode.has("district") ? rootNode.get("district").asText() : "",
                            rootNode.has("isp") ? rootNode.get("isp").asText() : "",
                            rootNode.has("areacode") ? rootNode.get("areacode").asText() : ""
                    );
                }

                throw new RuntimeException("IP 定位失败");
            }
        } catch (Exception e) {
            logger.error("IP 定位失败：{}", e.getMessage(), e);
            throw new RuntimeException("IP 定位失败：" + e.getMessage(), e);
        }
    }
}
