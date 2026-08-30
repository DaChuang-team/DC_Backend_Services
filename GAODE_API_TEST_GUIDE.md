# 高德 API 单元测试运行指南

## 📋 测试说明

本测试套件用于验证 `GaoDeService` 是否能成功连接并使用高德 API。

### 测试覆盖的功能

| 测试方法 | 测试功能 | 说明 |
|---------|---------|------|
| `testGeocode()` | 地理编码 | 测试地址转坐标（北京天安门） |
| `testReverseGeocode()` | 逆地理编码 | 测试坐标转地址（北京天安门） |
| `testRoutePlanning()` | 路径规划 | 测试驾车路线规划（天安门→故宫） |
| `testPOISearch()` | POI 搜索 | 测试景点搜索（北京故宫） |
| `testPOIAroundSearch()` | 周边搜索 | 测试周边美食搜索（故宫周边 1 公里） |
| `testIPLocation()` | IP 定位 | 测试指定 IP 定位（8.8.8.8） |
| `testIPLocationAuto()` | IP 自动定位 | 测试自动定位当前请求 IP |
| `testGeocodeShanghai()` | 地理编码 - 上海 | 测试上海地标（东方明珠） |
| `testGeocodeGuangzhou()` | 地理编码 - 广州 | 测试广州地标（广州塔） |

---

## 🚀 运行测试

### 方法 1：使用 Maven 命令行

```bash
# 运行所有测试
mvn test

# 只运行高德 API 测试
mvn test -Dtest=GaoDeServiceTest

# 运行单个测试方法
mvn test -Dtest=GaoDeServiceTest#testGeocode
```

### 方法 2：使用 IDE

#### IntelliJ IDEA
1. 打开 `GaoDeServiceTest.java`
2. 右键点击测试类或测试方法
3. 选择 `Run 'GaoDeServiceTest'` 或 `Debug 'GaoDeServiceTest'`

#### Eclipse
1. 打开 `GaoDeServiceTest.java`
2. 右键点击测试类
3. 选择 `Run As` → `JUnit Test`

#### VS Code
1. 打开 `GaoDeServiceTest.java`
2. 点击测试方法上方的 `Run Test` 或 `Debug Test`

---

## 📊 预期输出

### 成功示例

```
========== 测试地理编码（地址转坐标） ==========
✓ 地理编码测试通过
  地址：北京市天安门
  坐标：116.397526,39.908811
  格式化地址：北京市东城区景山前街 4 号

========== 测试逆地理编码（坐标转地址） ==========
✓ 逆地理编码测试通过
  坐标：116.397526,39.908811
  地址：北京市东城区景山前街 4 号
  省份：北京市
  城市：北京市
  区县：东城区

========== 测试路径规划（驾车） ==========
✓ 路径规划测试通过
  起点：116.397526,39.908811
  终点：116.397228,39.916691
  距离：1200 米
  耗时：180 秒
  步骤数：5

========== 测试 POI 搜索 ==========
✓ POI 搜索测试通过
  关键词：故宫
  城市：北京市
  找到 5 个结果
  第一个结果：
    名称：故宫博物院
    地址：北京市东城区景山前街 4 号
    坐标：116.397526,39.916691

========== 测试周边 POI 搜索 ==========
✓ 周边 POI 搜索测试通过
  关键词：美食
  中心点：116.397526,39.916691
  半径：1000 米
  找到 5 个结果
  第一个结果：
    名称：某某餐厅
    地址：北京市东城区某某胡同
    距离：200 米

========== 测试 IP 定位 ==========
✓ IP 定位测试通过
  IP 地址：8.8.8.8
  省份：加利福尼亚州
  城市：山景城
  坐标：-122.078515,37.405996
  运营商：Google LLC

========== 测试 IP 定位（自动定位） ==========
✓ IP 自动定位测试通过
  省份：浙江省
  城市：杭州市
  区县：西湖区
  坐标：120.15507,30.274085

========== 测试地理编码（上海） ==========
✓ 上海地理编码测试通过
  地址：上海东方明珠
  坐标：121.499143,31.239986
  格式化地址：上海市浦东新区陆家嘴世纪大道 1 号

========== 测试地理编码（广州） ==========
✓ 广州地理编码测试通过
  地址：广州塔
  坐标：113.322612,23.104289
  格式化地址：广东省广州市海珠区阅江西路 222 号
```

### 失败示例

```
========== 测试地理编码（地址转坐标） ==========
✗ 地理编码测试失败：地理编码失败：INVALID_USER_KEY
org.opentest4j.AssertionFailedError: 地理编码测试失败：地理编码失败：INVALID_USER_KEY
    at org.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:38)
    ...
```

---

## ⚠️ 常见问题

### 1. 提示 `INVALID_USER_KEY`

**原因**：高德 API Key 配置错误

**解决方法**：
```bash
# 检查环境变量是否配置
echo $GAODE_API_KEY  # Linux/Mac
echo %GAODE_API_KEY%  # Windows CMD
$env:GAODE_API_KEY  # Windows PowerShell

# 如果没有配置，设置环境变量
export GAODE_API_KEY=你的 API_Key  # Linux/Mac
$env:GAODE_API_KEY="你的 API_Key"  # Windows PowerShell
```

### 2. 提示 `IP 定位失败`

**原因**：
- API Key 未开通 IP 定位服务
- 网络问题导致请求超时

**解决方法**：
1. 登录高德开放平台，确认已开通"IP 定位"服务
2. 检查网络连接
3. 重试测试

### 3. 测试超时

**原因**：网络延迟或 API 响应慢

**解决方法**：
- 检查网络连接
- 重试测试
- 如持续超时，考虑增加超时配置

### 4. 提示 `PERMISSION_DENIED`

**原因**：API Key 的服务平台限制

**解决方法**：
1. 登录高德开放平台
2. 进入应用管理
3. 确认 Key 已启用"Web 服务"

---

## 📈 测试结果分析

### 全部测试通过 ✅

如果所有测试都通过，说明：
- ✅ API Key 配置正确
- ✅ 网络连接正常
- ✅ 所有高德 API 服务可用
- ✅ 代码逻辑正确

### 部分测试失败 ⚠️

如果部分测试失败：
1. 查看错误信息
2. 根据错误类型排查问题
3. 参考"常见问题"部分

### 全部测试失败 ❌

如果所有测试都失败，可能原因：
- ❌ API Key 未配置
- ❌ 网络连接问题
- ❌ 高德 API 服务不可用

**排查步骤**：
1. 检查 `application.properties` 中的配置
2. 检查环境变量
3. 测试网络连通性
4. 访问高德开放平台确认服务状态

---

## 🔧 自定义测试

### 添加新的测试用例

在 `GaoDeServiceTest.java` 中添加新的测试方法：

```java
@Test
void testYourCustomCase() {
    System.out.println("========== 测试你的自定义用例 ==========");
    
    // 准备测试数据
    String yourData = "测试数据";
    
    try {
        // 调用服务
        var response = gaoDeService.yourMethod(yourData);
        
        // 验证结果
        assertNotNull(response, "响应不应为空");
        // ... 其他验证
        
        System.out.println("✓ 自定义测试通过");
        
    } catch (Exception e) {
        System.err.println("✗ 自定义测试失败：" + e.getMessage());
        e.printStackTrace();
        fail("自定义测试失败：" + e.getMessage());
    }
}
```

### 修改测试数据

可以根据需要修改测试方法中的参数：

```java
// 修改测试地址
String address = "你的测试地址";
String city = "你的测试城市";

// 修改测试坐标
double longitude = 你的经度;
double latitude = 你的纬度;

// 修改搜索关键词
String keywords = "你的关键词";
```

---

## 📝 注意事项

1. **API 配额**：每次运行测试都会消耗 API 调用次数
2. **网络依赖**：测试需要联网才能执行
3. **真实数据**：测试使用真实的高德 API，返回真实数据
4. **测试环境**：建议在生产环境使用前充分测试
5. **错误处理**：测试中包含了完整的错误处理和日志输出

---

## 🎯 下一步

测试通过后，你可以：

1. ✅ 在控制器中放心使用 `GaoDeService`
2. ✅ 集成到业务逻辑中
3. ✅ 部署到生产环境
4. ✅ 添加更多自定义测试用例

---

## 📚 参考资料

- [高德开放平台](https://lbs.amap.com/)
- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
