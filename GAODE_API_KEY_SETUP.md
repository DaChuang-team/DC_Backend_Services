# 高德 API Key 配置指南

## ❌ 当前问题

测试失败，错误信息：
```
INVALID_USER_KEY
状态码：0
信息码：10002
SERVICE_NOT_AVAILABLE
```

## 🔍 原因分析

这个错误说明 API Key 本身格式正确，但高德 API 服务未正确配置。可能的原因：

1. **服务未开通** - 地理编码、POI 搜索等服务需要在高德平台手动开通
2. **Key 配置错误** - Key 的服务平台类型未选择"Web 服务"
3. **配额超限** - 每日调用次数达到上限
4. **Key 被禁用** - Key 可能因违规被禁用

## ✅ 解决步骤

### 步骤 1：登录高德开放平台

访问：https://console.amap.com/

使用你的账号登录。

### 步骤 2：检查应用和服务

1. 进入 **"应用管理"** → **"我的应用"**
2. 找到你创建的应用
3. 点击应用名称，查看 Key 的详情

### 步骤 3：开通所需服务

确保以下服务已开通：

| 服务名称 | 用途 | 测试方法 |
|---------|------|---------|
| **Web 服务** | 基础服务，必须开通 | ✅ |
| **地理编码** | 地址转坐标 | `testGeocode()` |
| **逆地理编码** | 坐标转地址 | `testReverseGeocode()` |
| **POI 搜索** | 搜索景点、酒店等 | `testPOISearch()` |
| **IP 定位** | 根据 IP 获取位置 | `testIPLocation()` |

**开通方法**：
1. 在应用详情页，找到对应服务
2. 点击"开通"或"启用"
3. 阅读并同意服务协议

### 步骤 4：检查 Key 配置

1. 进入 Key 的详情页
2. 确认 **"服务平台"** 已勾选 **"Web 服务"**
3. 检查 **"白名单"** 设置：
   - 如果设置了 IP 白名单，确保你的 IP 在列表中
   - 如果不确定，可以暂时移除白名单测试

### 步骤 5：检查配额

1. 进入 **"配额管理"**
2. 查看当日剩余调用次数
3. 如果配额用完，等待第二天或申请提升配额

### 步骤 6：重新测试

配置完成后，重新运行测试：

```bash
.\mvnw.cmd test -Dtest=GaoDeServiceSimpleTest
```

## 📊 预期结果

### 成功输出示例

```
✓ API Key 配置成功，长度：32
✓ API Key 前缀：695ba8de...
========== 测试地理编码（地址转坐标） ==========
使用的 API Key 前缀：695ba8de...
请求 URL: https://restapi.amap.com/v3/geocode?address=北京市天安门&key=***&city=北京市
响应内容：{"status":"1","info":"OK","infocode":"10000","count":"1","geocodes":[...]}
响应状态：1
返回信息：OK
返回信息码：10000
✓ 地理编码测试通过
  地址：北京市天安门
  坐标：116.397526,39.908811
  格式化地址：北京市东城区景山前街 4 号

[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 失败输出示例

```
✓ API Key 配置成功，长度：32
✓ API Key 前缀：695ba8de...
========== 测试地理编码（地址转坐标） ==========
使用的 API Key 前缀：695ba8de...
请求 URL: https://restapi.amap.com/v3/geocode?address=北京市天安门&key=***&city=北京市
响应内容：{"status":"0","info":"INVALID_USER_KEY","infocode":"10001"}
响应状态：0
返回信息：INVALID_USER_KEY
返回信息码：10001
✗ 地理编码测试失败：API 请求应该成功，状态码应为 1。实际状态码：0, 信息：INVALID_USER_KEY
```

## 🔧 常见错误码

| 错误码 | 说明 | 解决方法 |
|-------|------|---------|
| 10001 | INVALID_USER_KEY - Key 无效 | 检查 Key 是否正确复制 |
| 10002 | SERVICE_NOT_AVAILABLE - 服务不可用 | 开通对应服务 |
| 10003 | PERMISSION_DENIED - 权限不足 | 检查服务平台配置 |
| 10004 | OVER_QUOTA - 配额超限 | 等待第二天或提升配额 |
| 10005 | INVALID_SKEY - Key 被禁用 | 联系高德客服 |

## 📝 当前配置

你的 API Key 已配置在：
```
.vscode/settings.json
```

内容：
```json
{
    "java.env": {
        "GAODE_API_KEY": "695ba8de4ef377f5bbd6062312ba46e6"
    }
}
```

## 🎯 快速验证

使用 PowerShell 快速测试 API Key 是否有效：

```powershell
$apiKey = "695ba8de4ef377f5bbd6062312ba46e6"
$url = "https://restapi.amap.com/v3/geocode?address=北京市天安门&key=$apiKey&city=北京市"
$response = Invoke-WebRequest -Uri $url -Method Get
$json = $response.Content | ConvertFrom-Json

Write-Host "状态码：$($json.status)"
Write-Host "返回信息：$($json.info)"
Write-Host "信息码：$($json.infocode)"

if ($json.status -eq "1") {
    Write-Host "✓ API Key 有效！"
    $json.geocodes[0] | Format-List
} else {
    Write-Host "✗ API Key 无效，请检查配置"
}
```

## 📞 获取帮助

如果以上步骤都无法解决问题：

1. **查看高德官方文档**
   - https://lbs.amap.com/api/webservice/guide/api/geocode

2. **联系高德客服**
   - 论坛：https://lbs.amap.com/bbs/
   - 工单系统：https://console.amap.com/feedback/

3. **检查应用状态**
   - 确认应用未被封禁
   - 确认 Key 未被冻结

## ✅ 完成标志

当你看到以下输出时，说明配置成功：

```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

此时所有 4 个测试都应该通过：
- ✅ testGeocode() - 地理编码
- ✅ testReverseGeocode() - 逆地理编码  
- ✅ testPOISearch() - POI 搜索
- ✅ testIPLocation() - IP 定位
