/**
 * 本代码由 Muyvge 编写，仅用于学习交流，不得用于商业用途
 * 本代码的使用、复制、修改、合并、发布、分发、再许可、销售等行为均受到法律的严格限制。
 */
package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高德地图服务简单测试类
 * 不依赖 Spring 上下文，直接测试 API 连接
 */
class GaoDeServiceSimpleTest {

    private String apiKey;
    private String baseUrl;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 1. 首先尝试从系统属性获取（Maven 命令行方式）
        this.apiKey = System.getProperty("gaode.api.key");
        
        // 2. 如果系统属性没有，尝试从环境变量获取
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = System.getenv("GAODE_API_KEY");
        }
        
        // 3. 如果环境变量也没有，尝试从 .vscode/settings.json 读取
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            try {
                // 读取 .vscode/settings.json 文件
                java.io.File settingsFile = new java.io.File(".vscode/settings.json");
                if (settingsFile.exists()) {
                    String content = new String(java.nio.file.Files.readAllBytes(settingsFile.toPath()));
                    ObjectMapper tempMapper = new ObjectMapper();  // 临时创建 ObjectMapper
                    com.fasterxml.jackson.databind.JsonNode root = tempMapper.readTree(content);
                    
                    // 从 java.env 中获取 GAODE_API_KEY
                    JsonNode javaEnv = root.path("java.env");
                    if (javaEnv != null && !javaEnv.isMissingNode()) {
                        JsonNode gaodeKey = javaEnv.path("GAODE_API_KEY");
                        if (gaodeKey != null && !gaodeKey.isMissingNode()) {
                            this.apiKey = gaodeKey.asText();
                            System.out.println("✓ 已从 .vscode/settings.json 读取 API Key");
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠ 读取 .vscode/settings.json 失败：" + e.getMessage());
            }
        }
        
        // 4. 如果还是没有，使用默认值（测试会失败，但能看到错误信息）
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            this.apiKey = "your_test_api_key";
            System.err.println("⚠ 警告：未找到 API Key，将使用默认值（测试会失败）");
            System.err.println("请将 API Key 配置在以下位置之一：");
            System.err.println("  1. 系统属性：-Dgaode.api.key=你的 Key");
            System.err.println("  2. 环境变量：GAODE_API_KEY");
            System.err.println("  3. .vscode/settings.json 中的 java.env.GAODE_API_KEY");
        } else {
            System.out.println("✓ API Key 配置成功，长度：" + this.apiKey.length());
            System.out.println("✓ API Key 前缀：" + (this.apiKey.length() > 8 ? this.apiKey.substring(0, 8) + "..." : this.apiKey));
        }
        
        this.baseUrl = "https://restapi.amap.com/v3";
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 测试地理编码（地址转坐标）
     */
    @Test
    void testGeocode() {
        System.out.println("========== 测试地理编码（地址转坐标） ==========");
        System.out.println("使用的 API Key 前缀：" + (apiKey.length() > 8 ? apiKey.substring(0, 8) + "..." : apiKey));
        
        String address = "北京市天安门";
        String city = "北京市";
        
        try {
            // 构建请求 URL
            String url = String.format("%s/geocode/geo?address=%s&key=%s&city=%s",
                    baseUrl,
                    URLEncoder.encode(address, StandardCharsets.UTF_8),
                    apiKey,
                    URLEncoder.encode(city, StandardCharsets.UTF_8));

            System.out.println("请求 URL: " + url.replaceAll("key=[^&]+", "key=***"));

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                assertNotNull(response.body(), "响应体不应为空");
                
                String responseBody = response.body().string();
                System.out.println("响应内容：" + responseBody);
                
                JsonNode rootNode = objectMapper.readTree(responseBody);
                
                // 检查响应状态
                String status = rootNode.has("status") ? rootNode.get("status").asText() : "";
                String info = rootNode.has("info") ? rootNode.get("info").asText() : "";
                String infocode = rootNode.has("infocode") ? rootNode.get("infocode").asText() : "";
                
                System.out.println("响应状态：" + status);
                System.out.println("返回信息：" + info);
                System.out.println("返回信息码：" + infocode);
                
                // 验证响应成功
                assertEquals("1", status, "API 请求应该成功，状态码应为 1。实际状态码：" + status + ", 信息：" + info);
                
                // 解析地理编码结果
                if (rootNode.has("geocodes") && rootNode.get("geocodes").isArray()
                        && rootNode.get("geocodes").size() > 0) {
                    JsonNode geocode = rootNode.get("geocodes").get(0);
                    
                    String location = geocode.has("location") ? geocode.get("location").asText() : "";
                    String formattedAddress = geocode.has("formatted_address") ? 
                            geocode.get("formatted_address").asText() : "";
                    
                    System.out.println("✓ 地理编码测试通过");
                    System.out.println("  地址：" + address);
                    System.out.println("  坐标：" + location);
                    System.out.println("  格式化地址：" + formattedAddress);
                    
                    // 验证坐标不为空
                    assertFalse(location.isEmpty(), "坐标不应为空");
                    String[] parts = location.split(",");
                    assertEquals(2, parts.length, "坐标应包含经度和纬度");
                    
                    double longitude = Double.parseDouble(parts[0]);
                    double latitude = Double.parseDouble(parts[1]);
                    assertTrue(longitude > 0, "经度应大于 0");
                    assertTrue(latitude > 0, "纬度应大于 0");
                    
                } else {
                    fail("未找到地理编码结果");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ 地理编码测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("地理编码测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 IP 定位
     */
    @Test
    void testIPLocation() {
        System.out.println("========== 测试 IP 定位 ==========");
        
        String ip = "8.8.8.8";
        String type = "4";
        
        try {
            String url = String.format("%s/ip?ip=%s&key=%s&type=%s",
                    baseUrl,
                    URLEncoder.encode(ip, StandardCharsets.UTF_8),
                    apiKey,
                    type);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                assertNotNull(response.body(), "响应体不应为空");
                
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                
                // 检查响应状态
                String status = rootNode.has("status") ? rootNode.get("status").asText() : "";
                String info = rootNode.has("info") ? rootNode.get("info").asText() : "";
                
                System.out.println("响应状态：" + status);
                System.out.println("返回信息：" + info);
                
                // 验证响应成功
                assertEquals("1", status, "API 请求应该成功");
                
                // 解析 IP 定位结果
                String province = rootNode.has("province") ? rootNode.get("province").asText() : "";
                String city = rootNode.has("city") ? rootNode.get("city").asText() : "";
                String location = rootNode.has("location") ? rootNode.get("location").asText() : "";
                
                System.out.println("✓ IP 定位测试通过");
                System.out.println("  IP 地址：" + ip);
                System.out.println("  省份：" + province);
                System.out.println("  城市：" + city);
                System.out.println("  坐标：" + location);
                
                // 验证省份不为空（可能在国外，但应该有值）
                assertNotNull(province, "省份信息不应为空");
                
            }
        } catch (Exception e) {
            System.err.println("✗ IP 定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 POI 搜索
     */
    @Test
    void testPOISearch() {
        System.out.println("========== 测试 POI 搜索 ==========");
        
        String keywords = "故宫";
        String city = "北京市";
        String type = "风景名胜";
        
        try {
            String url = String.format("%s/place/text?keywords=%s&city=%s&types=%s&key=%s&offset=5&page=1",
                    baseUrl,
                    URLEncoder.encode(keywords, StandardCharsets.UTF_8),
                    URLEncoder.encode(city, StandardCharsets.UTF_8),
                    URLEncoder.encode(type, StandardCharsets.UTF_8),
                    apiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                assertNotNull(response.body(), "响应体不应为空");
                
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                
                String status = rootNode.has("status") ? rootNode.get("status").asText() : "";
                System.out.println("响应状态：" + status);
                
                assertEquals("1", status, "API 请求应该成功");
                
                if (rootNode.has("pois") && rootNode.get("pois").isArray()) {
                    JsonNode pois = rootNode.get("pois");
                    int count = pois.size();
                    
                    System.out.println("✓ POI 搜索测试通过");
                    System.out.println("  关键词：" + keywords);
                    System.out.println("  城市：" + city);
                    System.out.println("  找到 " + count + " 个结果");
                    
                    if (count > 0) {
                        JsonNode firstPoi = pois.get(0);
                        String name = firstPoi.has("name") ? firstPoi.get("name").asText() : "";
                        String address = firstPoi.has("address") ? firstPoi.get("address").asText() : "";
                        
                        System.out.println("  第一个结果：");
                        System.out.println("    名称：" + name);
                        System.out.println("    地址：" + address);
                        
                        assertFalse(name.isEmpty(), "POI 名称不应为空");
                    }
                } else {
                    fail("未找到 POI 结果");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ POI 搜索测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("POI 搜索测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试逆地理编码（坐标转地址）
     */
    @Test
    void testReverseGeocode() {
        System.out.println("========== 测试逆地理编码（坐标转地址） ==========");
        
        // 北京天安门坐标
        String location = "116.397526,39.908811";
        
        try {
            String url = String.format("%s/geocode/regeo?location=%s&key=%s",
                    baseUrl,
                    location,
                    apiKey);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                assertNotNull(response.body(), "响应体不应为空");
                
                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);
                
                String status = rootNode.has("status") ? rootNode.get("status").asText() : "";
                System.out.println("响应状态：" + status);
                
                assertEquals("1", status, "API 请求应该成功");
                
                if (rootNode.has("regeocode")) {
                    JsonNode regeocode = rootNode.get("regeocode");
                    String formattedAddress = regeocode.has("formatted_address") ? 
                            regeocode.get("formatted_address").asText() : "";
                    
                    System.out.println("✓ 逆地理编码测试通过");
                    System.out.println("  坐标：" + location);
                    System.out.println("  地址：" + formattedAddress);
                    
                    assertFalse(formattedAddress.isEmpty(), "格式化地址不应为空");
                } else {
                    fail("未找到逆地理编码结果");
                }
            }
        } catch (Exception e) {
            System.err.println("✗ 逆地理编码测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("逆地理编码测试失败：" + e.getMessage());
        }
    }
}
