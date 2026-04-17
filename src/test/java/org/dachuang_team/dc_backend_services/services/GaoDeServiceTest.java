/**
 * 本代码由 Muyvge 编写，仅用于学习交流，不得用于商业用途
 * 本代码的使用、复制、修改、合并、发布、分发、再许可、销售等行为均受到法律的严格限制。
 */
package org.dachuang_team.dc_backend_services.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dachuang_team.dc_backend_services.domain.DTO.GaoDeApiDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高德地图服务测试类
 * 使用纯 JUnit 测试，不依赖 Spring 上下文
 * 直接创建 GaoDeService 实例，调用真实的高德 API
 * 
 * 注意：此测试需要在 launch.json 中配置以下环境变量：
 * - GAODE_API_KEY: 高德地图 API Key
 */
class GaoDeServiceTest {

    private IGaoDeService gaoDeService;

    @BeforeEach
    void setUp() {
        // 从环境变量读取 API Key，如果读不到则使用默认值（仅用于测试）
        String apiKey = System.getenv("GAODE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // 如果环境变量未设置，使用默认值（仅用于本地测试）
            apiKey = "GAODE_API_KEY";
            System.out.println("⚠ 未找到环境变量 GAODE_API_KEY，使用默认值进行测试");
        }

        String baseUrl = "https://restapi.amap.com/v3";
        ObjectMapper objectMapper = new ObjectMapper();

        // 手动创建 GaoDeService 实例
        gaoDeService = new GaoDeService(apiKey, baseUrl, objectMapper);
    }

    /**
     * 测试地理编码（地址转坐标）
     */
    @Test
    void testGeocode() {
        System.out.println("========== 测试地理编码（地址转坐标） ==========");
        
        String address = "北京市天安门";
        String city = "北京市";
        
        try {
            GaoDeApiDTO.GeocodeResponse response = gaoDeService.geocode(address, city);
            
            assertNotNull(response, "响应不应为空");
            assertNotNull(response.location(), "坐标不应为空");
            assertFalse(response.location().isEmpty(), "坐标不应为空字符串");
            
            assertTrue(response.longitude() > 0, "经度应大于 0");
            assertTrue(response.latitude() > 0, "纬度应大于 0");
            
            assertNotNull(response.formattedAddress(), "格式化地址不应为空");
            assertFalse(response.formattedAddress().isEmpty(), "格式化地址不应为空字符串");
            
            System.out.println("✓ 地理编码测试通过");
            System.out.println("  地址：" + address);
            System.out.println("  坐标：" + response.location());
            System.out.println("  格式化地址：" + response.formattedAddress());
            
        } catch (Exception e) {
            System.err.println("✗ 地理编码测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("地理编码测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试逆地理编码（坐标转地址）
     */
    @Test
    void testReverseGeocode() {
        System.out.println("========== 测试逆地理编码（坐标转地址） ==========");
        
        double longitude = 116.397526;
        double latitude = 39.908811;
        String extensions = "base";
        
        try {
            GaoDeApiDTO.ReverseGeocodeResponse response = 
                    gaoDeService.reverseGeocode(longitude, latitude, extensions);
            
            assertNotNull(response, "响应不应为空");
            assertNotNull(response.formattedAddress(), "格式化地址不应为空");
            assertFalse(response.formattedAddress().isEmpty(), "格式化地址不应为空字符串");
            
            assertNotNull(response.province(), "省份不应为空");
            assertFalse(response.province().isEmpty(), "省份不应为空字符串");
            
            System.out.println("✓ 逆地理编码测试通过");
            System.out.println("  坐标：" + longitude + "," + latitude);
            System.out.println("  地址：" + response.formattedAddress());
            System.out.println("  省份：" + response.province());
            System.out.println("  城市：" + response.city());
            
        } catch (Exception e) {
            System.err.println("✗ 逆地理编码测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("逆地理编码测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试路径规划（驾车）
     */
    @Test
    void testRoutePlanning() {
        System.out.println("========== 测试路径规划（驾车） ==========");
        
        double originLongitude = 116.397526;
        double originLatitude = 39.908811;
        double destLongitude = 116.397228;
        double destLatitude = 39.916691;
        String type = "driving";
        String strategy = "0";
        
        try {
            GaoDeApiDTO.RoutePlanningResponse response = gaoDeService.routePlanning(
                    originLongitude, originLatitude,
                    destLongitude, destLatitude,
                    type, strategy);
            
            assertNotNull(response, "响应不应为空");
            assertNotNull(response.distance(), "距离不应为空");
            assertFalse(response.distance().isEmpty(), "距离不应为空字符串");
            
            int distance = Integer.parseInt(response.distance());
            assertTrue(distance > 0, "距离应大于 0");
            
            assertNotNull(response.duration(), "耗时不应为空");
            assertFalse(response.duration().isEmpty(), "耗时不应为空字符串");
            
            System.out.println("✓ 路径规划测试通过");
            System.out.println("  起点：" + originLongitude + "," + originLatitude);
            System.out.println("  终点：" + destLongitude + "," + destLatitude);
            System.out.println("  距离：" + distance + " 米");
            System.out.println("  耗时：" + response.duration() + " 秒");
            System.out.println("  步骤数：" + response.steps().size());
            
        } catch (Exception e) {
            System.err.println("✗ 路径规划测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("路径规划测试失败：" + e.getMessage());
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
        int page = 1;
        int pageSize = 5;
        
        try {
            var results = gaoDeService.poiSearch(keywords, city, type, page, pageSize);
            
            assertNotNull(results, "结果列表不应为空");
            assertTrue(results.size() > 0, "结果列表不应为空");
            
            System.out.println("✓ POI 搜索测试通过");
            System.out.println("  关键词：" + keywords);
            System.out.println("  城市：" + city);
            System.out.println("  找到 " + results.size() + " 个结果");
            
            if (!results.isEmpty()) {
                GaoDeApiDTO.POISearchResponse first = results.get(0);
                System.out.println("  第一个结果：");
                System.out.println("    名称：" + first.name());
                System.out.println("    地址：" + first.address());
                System.out.println("    坐标：" + first.location());
            }
            
        } catch (Exception e) {
            System.err.println("✗ POI 搜索测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("POI 搜索测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试周边 POI 搜索
     */
    @Test
    void testPOIAroundSearch() {
        System.out.println("========== 测试周边 POI 搜索 ==========");
        
        String keywords = "美食";
        double longitude = 116.397526;
        double latitude = 39.916691;
        String type = "餐饮服务";
        int radius = 1000;
        int page = 1;
        int pageSize = 5;
        
        try {
            var results = gaoDeService.poiAroundSearch(
                    keywords, longitude, latitude, type, radius, page, pageSize);
            
            assertNotNull(results, "结果列表不应为空");
            assertTrue(results.size() > 0, "结果列表不应为空");
            
            System.out.println("✓ 周边 POI 搜索测试通过");
            System.out.println("  关键词：" + keywords);
            System.out.println("  中心点：" + longitude + "," + latitude);
            System.out.println("  半径：" + radius + " 米");
            System.out.println("  找到 " + results.size() + " 个结果");
            
            if (!results.isEmpty()) {
                GaoDeApiDTO.POISearchResponse first = results.get(0);
                System.out.println("  第一个结果：");
                System.out.println("    名称：" + first.name());
                System.out.println("    地址：" + first.address());
                System.out.println("    距离：" + first.distance() + " 米");
            }
            
        } catch (Exception e) {
            System.err.println("✗ 周边 POI 搜索测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("周边 POI 搜索测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 IP 定位
     * 无法获取114.114.114.114（国内公共 DNS）的定位，原因推测：特殊用途 IP
     * 8.8.8.8（全球公共 DNS）Google DNS的定位也不行，原因推测：特殊用途 IP+国外 IP
     */
    @Test
    void testIPLocation() {
        System.out.println("========== 测试 IP 定位 ==========");
        
        // 使用百度服务器 IP（北京市）
        String ip = "220.181.38.148";
        String type = "4";
        
        try {
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            
            assertNotNull(response, "响应不应为空");
            
            System.out.println("✓ IP 定位测试通过");
            System.out.println("  IP 地址：" + response.ip());
            System.out.println("  省份：" + (response.province().isEmpty() ? "(空)" : response.province()));
            System.out.println("  城市：" + (response.city().isEmpty() ? "(空)" : response.city()));
            System.out.println("  区县：" + (response.district().isEmpty() ? "(空)" : response.district()));
            System.out.println("  坐标：" + (response.location().isEmpty() ? "(空)" : response.location()));
            System.out.println("  运营商：" + (response.isp().isEmpty() ? "(空)" : response.isp()));
            System.out.println("  国家：" + (response.country().isEmpty() ? "(空)" : response.country()));
            System.out.println("  城市编码：" + (response.adcode().isEmpty() ? "(空)" : response.adcode()));
            
        } catch (Exception e) {
            System.err.println("✗ IP 定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 IP 定位（自动定位当前请求 IP）
     * 注意：如果传入的是内网 IP（如 192.168.x.x, 172.16.x.x, 10.x.x.x），高德会返回"局域网"
     */
    @Test
    void testIPLocationAuto() {
        System.out.println("========== 测试 IP 定位（自动定位） ==========");
        
        // 使用本机公网 IP（广东省广州市）
        String ip = "58.248.26.171";
        String type = "4";
        
        try {
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            
            assertNotNull(response, "响应不应为空");
            
            // 自动定位会返回服务器 IP 的位置信息
            System.out.println("✓ IP 自动定位测试通过");
            System.out.println("  定位 IP：" + response.ip());
            System.out.println("  省份：" + (response.province().isEmpty() ? "(空)" : response.province()));
            System.out.println("  城市：" + (response.city().isEmpty() ? "(空)" : response.city()));
            System.out.println("  区县：" + (response.district().isEmpty() ? "(空)" : response.district()));
            System.out.println("  坐标：" + (response.location().isEmpty() ? "(空)" : response.location()));
            
        } catch (Exception e) {
            System.err.println("✗ IP 自动定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 自动定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 IP 定位 - 使用腾讯服务器 IP（深圳市）
     */
    @Test
    void testIPLocationTencent() {
        System.out.println("========== 测试 IP 定位（腾讯服务器） ==========");
        
        // 使用腾讯服务器 IP（深圳市）
        String ip = "183.3.226.35";
        String type = "4";
        
        try {
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            
            assertNotNull(response, "响应不应为空");
            
            System.out.println("✓ IP 定位测试通过");
            System.out.println("  IP 地址：" + ip);
            System.out.println("  省份：" + (response.province().isEmpty() ? "(空)" : response.province()));
            System.out.println("  城市：" + (response.city().isEmpty() ? "(空)" : response.city()));
            System.out.println("  区县：" + (response.district().isEmpty() ? "(空)" : response.district()));
            System.out.println("  坐标：" + (response.location().isEmpty() ? "(空)" : response.location()));
            System.out.println("  运营商：" + (response.isp().isEmpty() ? "(空)" : response.isp()));
            
        } catch (Exception e) {
            System.err.println("✗ IP 定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 IP 定位 - 自动获取当前请求设备的 IP
     * 注意：此测试不传 IP 参数，高德 API 会自动定位当前请求的 IP
     */
    @Test
    void testIPLocationAutoCurrentRequest() {
        System.out.println("========== 测试 IP 定位（自动获取当前请求 IP） ==========");
        
        try {
            // 不传 IP 参数，高德 API 会自动定位当前请求的 IP
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(null, "4");
            
            assertNotNull(response, "响应不应为空");
            
            System.out.println("✓ IP 自动定位测试通过");
            System.out.println("  定位 IP：" + response.ip());
            System.out.println("  省份：" + (response.province().isEmpty() ? "(空)" : response.province()));
            System.out.println("  城市：" + (response.city().isEmpty() ? "(空)" : response.city()));
            System.out.println("  区县：" + (response.district().isEmpty() ? "(空)" : response.district()));
            System.out.println("  坐标：" + (response.location().isEmpty() ? "(空)" : response.location()));
            System.out.println("  运营商：" + (response.isp().isEmpty() ? "(空)" : response.isp()));
            
        } catch (Exception e) {
            System.err.println("✗ IP 自动定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 自动定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试地理编码 - 上海地标
     * 有问题
     */
    @Test
    void testGeocodeShanghai() {
        System.out.println("========== 测试地理编码（上海） ==========");
        
        String address = "上海东方明珠";
        String city = "上海市";
        
        try {
            GaoDeApiDTO.GeocodeResponse response = gaoDeService.geocode(address, city);
            
            assertNotNull(response, "响应不应为空");
            assertNotNull(response.location(), "坐标不应为空");
            
            System.out.println("✓ 上海地理编码测试通过");
            System.out.println("  地址：" + address);
            System.out.println("  坐标：" + response.location());
            System.out.println("  格式化地址：" + response.formattedAddress());
            
        } catch (Exception e) {
            System.err.println("✗ 上海地理编码测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("上海地理编码测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试地理编码 - 广州地标
     */
    @Test
    void testGeocodeGuangzhou() {
        System.out.println("========== 测试地理编码（广州） ==========");
        
        String address = "广州塔";
        String city = "广州市";
        
        try {
            GaoDeApiDTO.GeocodeResponse response = gaoDeService.geocode(address, city);
            
            assertNotNull(response, "响应不应为空");
            assertNotNull(response.location(), "坐标不应为空");
            
            System.out.println("✓ 广州地理编码测试通过");
            System.out.println("  地址：" + address);
            System.out.println("  坐标：" + response.location());
            System.out.println("  格式化地址：" + response.formattedAddress());
            
        } catch (Exception e) {
            System.err.println("✗ 广州地理编码测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("广州地理编码测试失败：" + e.getMessage());
        }
    }
}
