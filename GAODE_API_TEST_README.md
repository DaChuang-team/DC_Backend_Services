# 高德 API 单元测试说明

## 📋 测试方案

由于项目依赖 Redis、OSS 等外部服务，完整的 Spring Boot 测试会遇到配置问题。因此提供了两种测试方案：

### 方案 1：简单测试（推荐）✅

**文件**: `GaoDeServiceSimpleTest.java`

**特点**:
- 不依赖 Spring 上下文
- 直接测试高德 API HTTP 接口
- 快速、独立
- 只需配置 API Key

**运行方法**:
```bash
# 方法 1：通过环境变量设置 API Key
$env:GAODE_API_KEY="你的高德 API Key"
.\mvnw.cmd test -Dtest=GaoDeServiceSimpleTest

# 方法 2：通过系统属性设置 API Key
.\mvnw.cmd test -Dtest=GaoDeServiceSimpleTest -Dgaode.api.key=你的高德 API Key
```

**测试覆盖**:
- ✅ 地理编码（地址转坐标）
- ✅ 逆地理编码（坐标转地址）
- ✅ POI 搜索
- ✅ IP 定位

### 方案 2：完整集成测试

**文件**: `GaoDeServiceTest.java`

**特点**:
- 加载完整 Spring 上下文
- 测试真实的服务层代码
- 需要配置所有依赖（数据库、Redis、OSS 等）

**运行方法**:
```bash
# 需要确保以下服务正常运行：
# 1. MySQL 数据库
# 2. Redis
# 3. 配置正确的 OSS 访问密钥
# 4. 配置正确的高德 API Key

.\mvnw.cmd test -Dtest=GaoDeServiceTest
```

---

## 🔑 获取高德 API Key

1. 访问 [高德开放平台](https://console.amap.com/)
2. 注册/登录账号
3. 进入"应用管理" → "我的应用"
4. 创建新应用
5. 添加 Key，选择"Web 服务"类型
6. 复制 Key 并在测试中使用

---

## 📊 测试结果说明

### 成功示例
```
========== 测试地理编码（地址转坐标） ==========
响应状态：1
返回信息：OK
✓ 地理编码测试通过
  地址：北京市天安门
  坐标：116.397526,39.908811
  格式化地址：北京市东城区景山前街 4 号
```

### 失败示例 - API Key 无效
```
========== 测试地理编码（地址转坐标） ==========
响应状态：0
返回信息：INVALID_USER_KEY
✗ 地理编码测试失败：API 请求应该成功，状态码应为 1 ==> expected: <1> but was: <0>
```

**解决方法**: 配置正确的 API Key

### 失败示例 - 网络问题
```
✗ 地理编码测试失败：timeout
```

**解决方法**: 检查网络连接

---

## ⚠️ 常见问题

### 1. 状态码为 0 - INVALID_USER_KEY
**原因**: API Key 无效或未配置

**解决**:
```bash
# 检查是否设置了 API Key
echo $env:GAODE_API_KEY

# 设置 API Key
$env:GAODE_API_KEY="你的高德 API Key"

# 重新运行测试
.\mvnw.cmd test -Dtest=GaoDeServiceSimpleTest
```

### 2. 状态码为 0 - PERMISSION_DENIED
**原因**: API Key 未开通相应服务

**解决**:
1. 登录高德开放平台
2. 进入应用管理
3. 确认已开通：地理编码、路径规划、POI 搜索、IP 定位等服务

### 3. 测试超时
**原因**: 网络连接问题

**解决**:
- 检查网络连接
- 重试测试
- 如持续超时，检查是否需要代理

### 4. 配额超限
**原因**: 高德 API 有每日调用次数限制

**解决**:
- 等待第二天再测试
- 或者升级高德 API 套餐

---

## 🎯 推荐测试流程

1. **获取 API Key**
   ```bash
   # 访问高德开放平台获取
   https://console.amap.com/
   ```

2. **设置环境变量**
   ```bash
   $env:GAODE_API_KEY="你的高德 API Key"
   ```

3. **运行简单测试**
   ```bash
   .\mvnw.cmd test -Dtest=GaoDeServiceSimpleTest
   ```

4. **查看测试结果**
   - 控制台会输出详细测试日志
   - 测试报告在 `target/surefire-reports/` 目录

---

## 📝 测试代码说明

### GaoDeServiceSimpleTest

```java
@Test
void testGeocode() {
    // 1. 构建 HTTP 请求
    String url = String.format("%s/geocode?address=%s&key=%s&city=%s",
            baseUrl, address, apiKey, city);
    
    // 2. 发送 HTTP 请求
    Request request = new Request.Builder().url(url).get().build();
    Response response = httpClient.newCall(request).execute();
    
    // 3. 解析响应
    String responseBody = response.body().string();
    JsonNode rootNode = objectMapper.readTree(responseBody);
    
    // 4. 验证状态码
    String status = rootNode.get("status").asText();
    assertEquals("1", status, "API 请求应该成功");
    
    // 5. 验证返回数据
    JsonNode geocode = rootNode.get("geocodes").get(0);
    String location = geocode.get("location").asText();
    assertFalse(location.isEmpty(), "坐标不应为空");
}
```

**关键点**:
1. 使用 OkHttp 发送 HTTP 请求
2. 使用 Jackson 解析 JSON 响应
3. 验证 API 状态码为 1（成功）
4. 验证返回数据的有效性

---

## 📈 测试覆盖率

| 功能 | 测试方法 | 状态 |
|------|---------|------|
| 地理编码 | `testGeocode()` | ✅ 已实现 |
| 逆地理编码 | `testReverseGeocode()` | ✅ 已实现 |
| POI 搜索 | `testPOISearch()` | ✅ 已实现 |
| IP 定位 | `testIPLocation()` | ✅ 已实现 |
| 路径规划 | 待实现 | ⏳ 待添加 |
| 周边 POI 搜索 | 待实现 | ⏳ 待添加 |

---

## 🔧 扩展测试

如需测试其他功能（如路径规划、周边搜索等），可以参考现有测试方法，在 `GaoDeServiceSimpleTest.java` 中添加新的测试方法：

```java
@Test
void testRoutePlanning() {
    System.out.println("========== 测试路径规划 ==========");
    
    String origin = "116.397526,39.908811";  // 起点
    String destination = "116.397228,39.916691";  // 终点
    String type = "driving";  // 驾车
    
    try {
        String url = String.format("%s/direction/driving?origin=%s&destination=%s&key=%s",
                baseUrl, origin, destination, apiKey);
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            
            String status = rootNode.has("status") ? 
                    rootNode.get("status").asText() : "";
            
            assertEquals("1", status, "API 请求应该成功");
            
            // 解析并验证路径规划结果
            if (rootNode.has("route") && 
                rootNode.get("route").has("paths") &&
                rootNode.get("route").get("paths").isArray() &&
                rootNode.get("route").get("paths").size() > 0) {
                
                JsonNode path = rootNode.get("route").get("paths").get(0);
                String distance = path.has("distance") ? 
                        path.get("distance").asText() : "";
                String duration = path.has("duration") ? 
                        path.get("duration").asText() : "";
                
                System.out.println("✓ 路径规划测试通过");
                System.out.println("  距离：" + distance + " 米");
                System.out.println("  耗时：" + duration + " 秒");
            } else {
                fail("未找到路径规划结果");
            }
        }
    } catch (Exception e) {
        System.err.println("✗ 路径规划测试失败：" + e.getMessage());
        e.printStackTrace();
        fail("路径规划测试失败：" + e.getMessage());
    }
}
```

---

## 📚 参考资料

- [高德地图 Web 服务 API 文档](https://lbs.amap.com/api/webservice/guide/api/geocode)
- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [OkHttp 官方文档](https://square.github.io/okhttp/)

---

## ✅ 总结

**推荐使用 `GaoDeServiceSimpleTest` 进行测试**，因为：
1. ✅ 配置简单，只需 API Key
2. ✅ 测试快速，不加载 Spring 上下文
3. ✅ 独立性强，不依赖其他服务
4. ✅ 直接测试 HTTP API，结果可靠

测试通过后，即可确认高德 API 集成成功，可以在项目中放心使用！🎉
