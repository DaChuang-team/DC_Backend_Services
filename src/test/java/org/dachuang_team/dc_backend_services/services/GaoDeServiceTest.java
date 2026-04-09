/**
 * 本代码由 Muyvge 编写，仅用于学习交流，不得用于商业用途
 * 本代码的使用、复制、修改、合并、发布、分发、再许可、销售等行为均受到法律的严格限制。
 */
package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.GaoDeApiDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高德地图服务测试类
 * 测试 GaoDeService 是否能成功连接并使用高德 API
 */
@SpringBootTest(properties = {
    "spring.config.import=classpath:application.properties",
    "aliyun.oss.access-key-id=test",
    "aliyun.oss.access-key-secret=test",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class GaoDeServiceTest {

    @Autowired
    private IGaoDeService gaoDeService;

    /**
     * 测试地理编码（地址转坐标）
     */
    @Test
    void testGeocode() {
        System.out.println("========== 测试地理编码（地址转坐标） ==========");
        
        // 测试地址：北京市天安门
        String address = "北京市天安门";
        String city = "北京市";
        
        try {
            GaoDeApiDTO.GeocodeResponse response = gaoDeService.geocode(address, city);
            
            // 验证响应不为空
            assertNotNull(response, "响应不应为空");
            
            // 验证坐标不为空
            assertNotNull(response.location(), "坐标不应为空");
            assertFalse(response.location().isEmpty(), "坐标不应为空字符串");
            
            // 验证经纬度合理
            assertTrue(response.longitude() > 0, "经度应大于 0");
            assertTrue(response.latitude() > 0, "纬度应大于 0");
            
            // 验证格式化地址不为空
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
        
        // 测试坐标：北京天安门附近
        double longitude = 116.397526;
        double latitude = 39.908811;
        String extensions = "base";
        
        try {
            GaoDeApiDTO.ReverseGeocodeResponse response = 
                    gaoDeService.reverseGeocode(longitude, latitude, extensions);
            
            // 验证响应不为空
            assertNotNull(response, "响应不应为空");
            
            // 验证格式化地址不为空
            assertNotNull(response.formattedAddress(), "格式化地址不应为空");
            assertFalse(response.formattedAddress().isEmpty(), "格式化地址不应为空字符串");
            
            // 验证省份不为空
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
        
        // 测试路线：从天安门到故宫
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
            
            // 验证响应不为空
            assertNotNull(response, "响应不应为空");
            
            // 验证距离不为空
            assertNotNull(response.distance(), "距离不应为空");
            assertFalse(response.distance().isEmpty(), "距离不应为空字符串");
            
            // 验证距离大于 0
            int distance = Integer.parseInt(response.distance());
            assertTrue(distance > 0, "距离应大于 0");
            
            // 验证耗时不为空
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
            
            // 验证结果不为空
            assertNotNull(results, "结果列表不应为空");
            
            // 验证结果数量
            assertTrue(results.size() > 0, "结果列表不应为空");
            System.out.println("✓ POI 搜索测试通过");
            System.out.println("  关键词：" + keywords);
            System.out.println("  城市：" + city);
            System.out.println("  找到 " + results.size() + " 个结果");
            
            // 打印第一个结果
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
        double longitude = 116.397526;  // 故宫附近
        double latitude = 39.916691;
        String type = "餐饮服务";
        int radius = 1000;  // 1 公里
        int page = 1;
        int pageSize = 5;
        
        try {
            var results = gaoDeService.poiAroundSearch(
                    keywords, longitude, latitude, type, radius, page, pageSize);
            
            // 验证结果不为空
            assertNotNull(results, "结果列表不应为空");
            
            // 验证结果数量
            assertTrue(results.size() > 0, "结果列表不应为空");
            System.out.println("✓ 周边 POI 搜索测试通过");
            System.out.println("  关键词：" + keywords);
            System.out.println("  中心点：" + longitude + "," + latitude);
            System.out.println("  半径：" + radius + " 米");
            System.out.println("  找到 " + results.size() + " 个结果");
            
            // 打印第一个结果
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
     */
    @Test
    void testIPLocation() {
        System.out.println("========== 测试 IP 定位 ==========");
        
        // 测试公共 DNS：8.8.8.8
        String ip = "8.8.8.8";
        String type = "4";
        
        try {
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            
            // 验证响应不为空
            assertNotNull(response, "响应不应为空");
            
            // 验证省份不为空（可能在国外，但应该有值）
            assertNotNull(response.province(), "省份不应为空");
            
            // 验证经纬度
            assertTrue(response.longitude() != 0 || response.latitude() != 0, 
                    "经纬度至少有一个不应为 0");
            
            System.out.println("✓ IP 定位测试通过");
            System.out.println("  IP 地址：" + ip);
            System.out.println("  省份：" + response.province());
            System.out.println("  城市：" + response.city());
            System.out.println("  坐标：" + response.location());
            System.out.println("  运营商：" + response.isp());
            
        } catch (Exception e) {
            System.err.println("✗ IP 定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试 IP 定位（自动定位当前请求 IP）
     */
    @Test
    void testIPLocationAuto() {
        System.out.println("========== 测试 IP 定位（自动定位） ==========");
        
        String ip = null;  // 不传 IP，自动定位
        String type = "4";
        
        try {
            GaoDeApiDTO.IPLocationResponse response = gaoDeService.ipLocation(ip, type);
            
            // 验证响应不为空
            assertNotNull(response, "响应不应为空");
            
            // 验证省份不为空
            assertNotNull(response.province(), "省份不应为空");
            assertFalse(response.province().isEmpty(), "省份不应为空字符串");
            
            System.out.println("✓ IP 自动定位测试通过");
            System.out.println("  省份：" + response.province());
            System.out.println("  城市：" + response.city());
            System.out.println("  区县：" + response.district());
            System.out.println("  坐标：" + response.location());
            
        } catch (Exception e) {
            System.err.println("✗ IP 自动定位测试失败：" + e.getMessage());
            e.printStackTrace();
            fail("IP 自动定位测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试地理编码 - 上海地标
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
