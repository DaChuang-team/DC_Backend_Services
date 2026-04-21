# 高德地图 API 接口说明文档

## 文档说明

本文档描述了高德地图服务与前端对接的所有 RESTful API 接口，包括接口 URL、功能说明、请求参数、响应参数及使用示例。

**基础路径**: `/api/gaode`

**坐标系统**: GCJ-02（火星坐标系）

**坐标格式**: 所有坐标参数均使用 `经度，纬度` 格式，中间用英文逗号分隔

---

## 目录

1. [地理编码接口（地址转坐标）](#1-地理编码接口地址转坐标)
2. [逆地理编码接口（坐标转地址）](#2-逆地理编码接口坐标转地址)
3. [路径规划接口](#3-路径规划接口)
4. [POI 搜索接口](#4-poi-搜索接口)
5. [周边 POI 搜索接口](#5-周边-poi-搜索接口)
6. [IP 定位接口](#6-ip-定位接口)
7. [状态码说明](#状态码说明)
8. [通用响应格式](#通用响应格式)

---

## 1. 地理编码接口（地址转坐标）

### 接口信息
- **URL**: `/api/gaode/geocode`
- **功能**: 将详细地址转换为地理坐标（经纬度）
- **请求方式**: `GET`

### 请求参数

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| address | String | 地址 | 是 | 要解析的详细地址 |
| city | String | 城市名称 | 否 | 指定城市可提高解析精度 |

### 请求示例

```http
GET /api/gaode/geocode?address=北京市朝阳区天安门&city=北京市 HTTP/1.1
Host: localhost:8080
```

### 响应参数（成功 - 200）

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| code | int | 状态码 | 是 | 200 表示成功 |
| message | String | 消息 | 是 | 成功描述 |
| data.address | String | 原始地址 | 是 | 请求的地址 |
| data.location | String | 坐标 | 是 | 格式：经度，纬度 |
| data.longitude | double | 经度 | 是 | 经度值 |
| data.latitude | double | 纬度 | 是 | 纬度值 |
| data.formattedAddress | String | 格式化地址 | 是 | 标准化后的地址 |

### 响应示例（成功 - 200）

```json
{
  "code": 200,
  "message": "地理编码成功",
  "data": {
    "address": "北京市朝阳区天安门",
    "location": "116.397428,39.90923",
    "longitude": 116.397428,
    "latitude": 39.90923,
    "formattedAddress": "北京市东城区景山前街 4 号"
  }
}
```

### 响应示例（参数错误 - 400）

```json
{
  "code": 400,
  "message": "地址不能为空",
  "data": null
}
```

### 响应示例（失败 - 500）

```json
{
  "code": 500,
  "message": "地理编码失败：未找到地址的地理编码",
  "data": null
}
```

---

## 2. 逆地理编码接口（坐标转地址）

### 接口信息
- **URL**: `/api/gaode/reverseGeocode`
- **功能**: 将地理坐标转换为详细地址信息
- **请求方式**: `GET`

### 请求参数

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| longitude | double | 经度 | 是 | 地理经度值 |
| latitude | double | 纬度 | 是 | 地理纬度值 |
| extensions | String | 扩展信息 | 否 | base（基础）/all（详细），默认：base |

### 请求示例

```http
GET /api/gaode/reverseGeocode?longitude=116.397428&latitude=39.90923&extensions=all HTTP/1.1
Host: localhost:8080
```

### 响应参数（成功 - 200）

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| code | int | 状态码 | 是 | 200 表示成功 |
| message | String | 消息 | 是 | 成功描述 |
| data.formattedAddress | String | 格式化地址 | 是 | 完整地址描述 |
| data.addressComponent.province | String | 省份 | 是 | 省份名称 |
| data.addressComponent.city | String | 城市 | 是 | 城市名称 |
| data.addressComponent.citycode | String | 城市编码 | 是 | 城市代码 |
| data.addressComponent.district | String | 区县 | 是 | 区县名称 |
| data.addressComponent.towncode | String | 乡镇编码 | 是 | 乡镇代码 |
| data.addressComponent.township | String | 乡镇 | 是 | 乡镇名称 |
| data.addressComponent.neighborhood | String | 社区 | 否 | 社区/小区名称 |
| data.addressComponent.building | String | 建筑物 | 否 | 建筑物名称 |
| data.addressComponent.streetNumber | String | 门牌号 | 否 | 门牌号码 |
| data.addressComponent.street | String | 街道 | 否 | 街道名称 |
| data.city | String | 城市 | 是 | 城市名称 |
| data.district | String | 区县 | 是 | 区县名称 |
| data.province | String | 省份 | 是 | 省份名称 |
| data.streetNumber | String | 门牌号 | 否 | 门牌号码 |
| data.neighborhood | String | 周边信息 | 否 | 周边环境描述 |
| data.poiName | String | 附近 POI 名称 | 否 | 附近兴趣点名称 |
| data.poiType | String | 附近 POI 类型 | 否 | 附近兴趣点类型 |

### 响应示例（成功 - 200）

```json
{
  "code": 200,
  "message": "逆地理编码成功",
  "data": {
    "formattedAddress": "北京市东城区景山前街 4 号",
    "addressComponent": {
      "province": "北京市",
      "city": "北京市",
      "citycode": "010",
      "district": "东城区",
      "towncode": "110101001",
      "township": "东华门街道",
      "neighborhood": "故宫社区",
      "building": "",
      "streetNumber": "4 号",
      "street": "景山前街"
    },
    "city": "北京市",
    "district": "东城区",
    "province": "北京市",
    "streetNumber": "4 号",
    "neighborhood": "故宫社区",
    "poiName": "故宫博物院",
    "poiType": "风景名胜;博物馆"
  }
}
```

### 响应示例（失败 - 500）

```json
{
  "code": 500,
  "message": "逆地理编码失败：坐标超出范围",
  "data": null
}
```

---

## 3. 路径规划接口

### 接口信息
- **URL**: `/api/gaode/route`
- **功能**: 规划两点之间的路径，支持驾车、步行、公交等多种方式
- **请求方式**: `GET`

### 请求参数

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| originLongitude | double | 起点经度 | 是 | 起点地理经度（来自前端 GPS 定位） |
| originLatitude | double | 起点纬度 | 是 | 起点地理纬度（来自前端 GPS 定位） |
| destinationLongitude | double | 终点经度 | 是 | 终点地理经度 |
| destinationLatitude | double | 终点纬度 | 是 | 终点地理纬度 |
| type | String | 路径类型 | 否 | driving（驾车）/walking（步行）/transit（公交），默认：driving |
| strategy | String | 规划策略 | 否 | 0（速度优先）/1（费用优先）/2（距离优先）/3（舒适优先），默认：0 |

### 请求示例

```http
GET /api/gaode/route?originLongitude=116.397428&originLatitude=39.90923&destinationLongitude=116.407428&destinationLatitude=39.91923&type=driving&strategy=0 HTTP/1.1
Host: localhost:8080
```

### 响应参数（成功 - 200）

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| code | int | 状态码 | 是 | 200 表示成功 |
| message | String | 消息 | 是 | 成功描述 |
| data.distance | String | 距离 | 是 | 总距离（米） |
| data.duration | String | 耗时 | 是 | 总耗时（秒） |
| data.distanceText | String | 格式化距离 | 是 | 如："1.23 公里" |
| data.durationText | String | 格式化时长 | 是 | 如："1 小时 30 分钟" |
| data.startLocation | String | 起点坐标 | 是 | 格式：经度，纬度 |
| data.endLocation | String | 终点坐标 | 是 | 格式：经度，纬度 |
| data.steps | Array | 路径步骤 | 是 | 详细路径步骤数组 |
| data.steps[].instruction | String | 行走指令 | 是 | 导航指令描述 |
| data.steps[].distance | String | 本段距离 | 是 | 该步骤距离（米） |
| data.steps[].duration | String | 本段耗时 | 是 | 该步骤耗时（秒） |
| data.steps[].distanceText | String | 格式化距离 | 是 | 如："500 米" |
| data.steps[].durationText | String | 格式化时长 | 是 | 如："5 分钟" |
| data.steps[].startLocation | String | 起点坐标 | 是 | 该步骤起点 |
| data.steps[].endLocation | String | 终点坐标 | 是 | 该步骤终点 |
| data.steps[].action | String | 动作 | 是 | 如："左转"、"右转"、"直行" |
| data.steps[].assistantAction | String | 辅助动作 | 否 | 如："靠右行驶"、"进入环岛" |

### 响应示例（成功 - 200）

```json
{
  "code": 200,
  "message": "路径规划成功",
  "data": {
    "distance": "1500",
    "duration": "300",
    "distanceText": "1.5 公里",
    "durationText": "5 分钟",
    "startLocation": "116.397428,39.90923",
    "endLocation": "116.407428,39.91923",
    "steps": [
      {
        "instruction": "从起点向东北方向行驶",
        "distance": "200",
        "duration": "40",
        "distanceText": "200 米",
        "durationText": "40 秒",
        "startLocation": "116.397428,39.90923",
        "endLocation": "116.398428,39.91023",
        "action": "直行",
        "assistantAction": ""
      },
      {
        "instruction": "右转进入东长安街",
        "distance": "800",
        "duration": "160",
        "distanceText": "800 米",
        "durationText": "2 分钟 40 秒",
        "startLocation": "116.398428,39.91023",
        "endLocation": "116.405428,39.91523",
        "action": "右转",
        "assistantAction": "靠右行驶"
      },
      {
        "instruction": "到达终点",
        "distance": "500",
        "duration": "100",
        "distanceText": "500 米",
        "durationText": "1 分钟 40 秒",
        "startLocation": "116.405428,39.91523",
        "endLocation": "116.407428,39.91923",
        "action": "直行",
        "assistantAction": ""
      }
    ]
  }
}
```

### 响应示例（参数错误 - 400）

```json
{
  "code": 400,
  "message": "起点坐标无效，请检查 GPS 定位数据",
  "data": null
}
```

或

```json
{
  "code": 400,
  "message": "终点坐标无效",
  "data": null
}
```

### 响应示例（失败 - 500）

```json
{
  "code": 500,
  "message": "路径规划失败：无法规划该路线",
  "data": null
}
```

---

## 4. POI 搜索接口

### 接口信息
- **URL**: `/api/gaode/poi/search`
- **功能**: 搜索指定城市内的兴趣点（POI），如景点、餐厅、酒店等
- **请求方式**: `GET`

### 请求参数

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| keywords | String | 搜索关键词 | 是 | 搜索内容，如"故宫"、"餐厅" |
| city | String | 城市名称 | 否 | 指定搜索城市 |
| type | String | POI 类型 | 否 | POI 分类代码，如"110000"（风景名胜） |
| page | int | 页码 | 否 | 页码，从 1 开始，默认：1 |
| pageSize | int | 每页数量 | 否 | 每页返回数量，默认：10 |

### 请求示例

```http
GET /api/gaode/poi/search?keywords=故宫&city=北京市&type=110000&page=1&pageSize=10 HTTP/1.1
Host: localhost:8080
```

### 响应参数（成功 - 200）

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| code | int | 状态码 | 是 | 200 表示成功 |
| message | String | 消息 | 是 | 成功描述 |
| data | Array | POI 列表 | 是 | POI 信息数组 |
| data[].poiId | String | POI ID | 是 | 唯一标识符 |
| data[].name | String | 名称 | 是 | POI 名称 |
| data[].type | String | 类型 | 是 | POI 类型描述 |
| data[].typecode | String | 类型编码 | 是 | POI 类型代码 |
| data[].address | String | 地址 | 是 | 详细地址 |
| data[].location | String | 坐标 | 是 | 格式：经度，纬度 |
| data[].longitude | double | 经度 | 是 | 经度值 |
| data[].latitude | double | 纬度 | 是 | 纬度值 |
| data[].tel | String | 电话 | 否 | 联系电话 |
| data[].distance | String | 距离 | 否 | 距参考点距离（米） |
| data[].rating | String | 评分 | 否 | 用户评分 |
| data[].bizExt | String | 商业信息 | 否 | 商业扩展信息 |
| data[].timestamp | String | 时间戳 | 是 | 数据时间戳 |

### 响应示例（成功 - 200）

```json
{
  "code": 200,
  "message": "POI 搜索成功",
  "data": [
    {
      "poiId": "B000A0BD6A",
      "name": "故宫博物院",
      "type": "风景名胜;博物馆",
      "typecode": "110000",
      "address": "北京市东城区景山前街 4 号",
      "location": "116.397428,39.91923",
      "longitude": 116.397428,
      "latitude": 39.91923,
      "tel": "010-85007421",
      "distance": "",
      "rating": "5.0",
      "bizExt": "营业时间：08:30-17:00",
      "timestamp": "2024-01-15 10:30:00"
    },
    {
      "poiId": "B000A0BD6B",
      "name": "景山公园",
      "type": "风景名胜;公园",
      "typecode": "110100",
      "address": "北京市西城区景山西街 44 号",
      "location": "116.395428,39.92123",
      "longitude": 116.395428,
      "latitude": 39.92123,
      "tel": "010-64062285",
      "distance": "",
      "rating": "4.8",
      "bizExt": "营业时间：06:30-20:00",
      "timestamp": "2024-01-15 10:30:00"
    }
  ]
}
```

### 响应示例（参数错误 - 400）

```json
{
  "code": 400,
  "message": "搜索关键词不能为空",
  "data": null
}
```

### 响应示例（失败 - 500）

```json
{
  "code": 500,
  "message": "POI 搜索失败：网络请求超时",
  "data": null
}
```

---

## 5. 周边 POI 搜索接口

### 接口信息
- **URL**: `/api/gaode/poi/around`
- **功能**: 搜索指定位置周边的兴趣点（POI）
- **请求方式**: `GET`

### 请求参数

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| keywords | String | 搜索关键词 | 是 | 搜索内容，如"餐厅"、"酒店" |
| longitude | double | 中心点经度 | 是 | 中心点地理经度（来自前端 GPS 定位） |
| latitude | double | 中心点纬度 | 是 | 中心点地理纬度（来自前端 GPS 定位） |
| type | String | POI 类型 | 否 | POI 分类代码 |
| radius | int | 搜索半径 | 否 | 搜索范围（米），范围 100-5000，默认：1000 |
| page | int | 页码 | 否 | 页码，从 1 开始，默认：1 |
| pageSize | int | 每页数量 | 否 | 每页返回数量，默认：10 |

### 请求示例

```http
GET /api/gaode/poi/around?keywords=餐厅&longitude=116.397428&latitude=39.91923&type=050000&radius=1000&page=1&pageSize=10 HTTP/1.1
Host: localhost:8080
```

### 响应参数（成功 - 200）

与 POI 搜索接口相同，参考接口 4 的响应参数。

### 响应示例（成功 - 200）

```json
{
  "code": 200,
  "message": "周边 POI 搜索成功",
  "data": [
    {
      "poiId": "B000A1234C",
      "name": "老北京炸酱面",
      "type": "餐饮服务;中餐厅",
      "typecode": "050100",
      "address": "北京市东城区东华门大街 10 号",
      "location": "116.398428,39.92023",
      "longitude": 116.398428,
      "latitude": 39.92023,
      "tel": "010-12345678",
      "distance": "150",
      "rating": "4.5",
      "bizExt": "人均消费：50 元",
      "timestamp": "2024-01-15 11:00:00"
    },
    {
      "poiId": "B000A1234D",
      "name": "星巴克咖啡",
      "type": "餐饮服务;咖啡厅",
      "typecode": "050200",
      "address": "北京市东城区王府井大街 88 号",
      "location": "116.399428,39.92123",
      "longitude": 116.399428,
      "latitude": 39.92123,
      "tel": "010-87654321",
      "distance": "280",
      "rating": "4.7",
      "bizExt": "人均消费：35 元",
      "timestamp": "2024-01-15 11:00:00"
    }
  ]
}
```

### 响应示例（参数错误 - 400）

```json
{
  "code": 400,
  "message": "搜索关键词不能为空",
  "data": null
}
```

或

```json
{
  "code": 400,
  "message": "GPS 坐标无效，请检查定位数据",
  "data": null
}
```

### 响应示例（失败 - 500）

```json
{
  "code": 500,
  "message": "周边 POI 搜索失败：网络请求超时",
  "data": null
}
```

---

## 6. IP 定位接口

### 接口信息
- **URL**: `/api/gaode/ip`
- **功能**: 根据 IP 地址定位用户位置（**已弃置保留**，建议使用前端 GPS 定位）
- **请求方式**: `GET`

### 请求参数

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| ip | String | IP 地址 | 否 | 要定位的 IP 地址，不传则自动定位当前请求 IP |
| type | String | 定位类型 | 否 | 固定为 4（仅支持 IPv4），默认值：4 |

### 请求示例

```http
GET /api/gaode/ip?type=4 HTTP/1.1
Host: localhost:8080
```

或指定 IP：

```http
GET /api/gaode/ip?ip=8.8.8.8&type=4 HTTP/1.1
Host: localhost:8080
```

### 响应参数（成功 - 200）

| 参数名 | 类型 | 中文名 | 是否必需 | 说明 |
|--------|------|--------|----------|------|
| code | int | 状态码 | 是 | 200 表示成功 |
| message | String | 消息 | 是 | 成功描述 |
| data.ip | String | IP 地址 | 是 | 定位的 IP 地址 |
| data.location | String | 坐标 | 是 | 格式：经度，纬度 |
| data.longitude | double | 经度 | 是 | 经度值 |
| data.latitude | double | 纬度 | 是 | 纬度值 |
| data.province | String | 省份 | 是 | 省份名称 |
| data.city | String | 城市 | 是 | 城市名称 |
| data.adcode | String | 城市编码 | 是 | 行政区划代码 |
| data.district | String | 区县 | 是 | 区县名称 |
| data.isp | String | 运营商 | 是 | 网络服务提供商 |
| data.areaCode | String | 区域代码 | 是 | 电话区号 |
| data.country | String | 国家 | 是 | 国家名称 |
| data.cityNameCode | String | 城市名称代码 | 是 | 城市标准代码 |

### 响应示例（成功 - 200）

```json
{
  "code": 200,
  "message": "IP 定位成功",
  "data": {
    "ip": "8.8.8.8",
    "location": "116.481488,39.990464",
    "longitude": 116.481488,
    "latitude": 39.990464,
    "province": "北京市",
    "city": "北京市",
    "adcode": "110000",
    "district": "朝阳区",
    "isp": "电信",
    "areaCode": "010",
    "country": "中国",
    "cityNameCode": "110100"
  }
}
```

### 响应示例（参数错误 - 400）

```json
{
  "code": 400,
  "message": "IP 地址格式不正确",
  "data": null
}
```

### 响应示例（失败 - 500）

```json
{
  "code": 500,
  "message": "IP 定位失败：网络连接超时",
  "data": null
}
```

---

## 状态码说明

| 状态码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 请求成功 | 正常处理返回数据 |
| 400 | 请求参数错误 | 检查请求参数格式和必填项，如坐标无效、关键词为空等 |
| 500 | 服务器内部错误 | 联系后端开发人员或稍后重试 |

---

## 通用响应格式

所有接口均使用统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 业务状态码，200 表示成功，其他表示失败 |
| message | String | 响应消息，成功时为成功描述，失败时为错误原因 |
| data | Object/Array/null | 响应数据，成功时返回业务数据，失败时为 null |

---

## 注意事项

### 1. 坐标系统
- 高德地图使用 **GCJ-02 坐标系**（火星坐标系）
- 与其他地图系统（如 WGS-84）可能存在偏差
- 前端 GPS 定位获取的坐标需转换为 GCJ-02 坐标系后再传给后端

### 2. 坐标格式
- 所有坐标参数均使用 `经度，纬度` 格式
- 经度范围：-180 到 180
- 纬度范围：-90 到 90
- 后端会验证坐标有效性，无效坐标将返回 400 错误

### 3. 分页
- POI 搜索接口支持分页
- 页码从 1 开始
- 默认每页 10 条数据

### 4. 搜索半径
- 周边 POI 搜索的半径范围为 100-5000 米
- 超出范围将自动调整为边界值

### 5. 路径规划
- 起点和终点坐标必须有效
- 建议使用前端 GPS 定位获取起点坐标
- 支持多种路径类型和规划策略

### 6. 错误处理
- 前端应统一处理 400 和 500 错误
- 给用户友好的提示信息
- 建议记录错误日志便于排查问题

### 7. IP 定位接口说明
- IP 定位接口已弃置保留
- 精度较低，建议使用前端 GPS 定位
- 仅在无法获取 GPS 定位时作为备选方案

---

## 前端开发建议

### GPS 定位获取示例（JavaScript）

```javascript
// 使用 HTML5 Geolocation API 获取用户位置
function getUserLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持地理定位'));
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          longitude: position.coords.longitude,
          latitude: position.coords.latitude
        });
      },
      (error) => {
        reject(new Error(`定位失败：${error.message}`));
      },
      {
        enableHighAccuracy: true,  // 使用高精度模式
        timeout: 10000,            // 超时时间 10 秒
        maximumAge: 0              // 不使用缓存
      }
    );
  });
}

// 使用示例
getUserLocation()
  .then(location => {
    console.log('用户位置:', location);
    // 调用路径规划或周边搜索接口
  })
  .catch(error => {
    console.error('定位失败:', error);
  });
```

### 坐标转换提示

如果前端获取的是 WGS-84 坐标（GPS 原始坐标），需要转换为 GCJ-02 坐标系：

```javascript
// WGS-84 转 GCJ-02 的简单实现
function transformWGS84ToGCJ02(wgLat, wgLon) {
  const a = 6378245.0;
  const ee = 0.00669342162296594323;
  
  if (outOfChina(wgLat, wgLon)) {
    return { latitude: wgLat, longitude: wgLon };
  }
  
  const dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
  const dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
  const radLat = wgLat / 180.0 * Math.PI;
  const magic = Math.sin(radLat);
  magic = 1 - ee * magic * magic;
  const sqrtMagic = Math.sqrt(magic);
  
  const gcjLat = wgLat + (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
  const gcjLon = wgLon + (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
  
  return { latitude: gcjLat, longitude: gcjLon };
}

function outOfChina(lat, lon) {
  return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
}

function transformLat(x, y) {
  let ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
  ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
  ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
  ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
  return ret;
}

function transformLon(x, y) {
  let ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
  ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
  ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
  ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
  return ret;
}
```

---

## 技术支持

如有问题，请联系开发团队或查看后端日志获取详细错误信息。

---

**文档版本**: v2.0  
**最后更新**: 2024 年  
**维护团队**: 大创团队
