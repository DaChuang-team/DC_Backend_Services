# 后端修改记录

用途：
- 记录 `DC_Backend_Services` 的所有后端修改，方便回溯、联调和交接。
- 以后只要修改后端代码，就先在这里补一条记录，再提交代码。

维护规则：
- 按时间倒序追加，最新记录放在最上面。
- 每条记录尽量包含：日期、修改目的、涉及文件、核心改动、影响范围、验证结果。
- 如果一次修改跨多个文件，优先写清楚文件列表和接口变化。

记录模板：

```md
## YYYY-MM-DD
- 修改目的：
- 涉及文件：
- 核心改动：
- 影响范围：
- 验证结果：
```

---

## 2026-05-30 (4)
- 修改目的：重构日志系统，移除旧的登录日志，搭建基于 AOP 的统一操作日志体系（业务日志 + 系统日志），实现每天自动清理15天前的旧日志
- 涉及文件：
  - **删除：** `LoginLog.java`、`LoginLogRepository.java`
  - **新增：** `OperationLog.java`、`OperationLogRepository.java`、`LogAspect.java`、`BusinessLog.java`、`SystemLog.java`、`LogService.java`、`LogCleanupTask.java`
  - **修改：** `IAdminService.java`、`AdminService.java`、`AdminController.java`、`SecurityConfig.java`、`OrderService.java`
- 核心改动：
  1. **移除旧登录日志**：删除 LoginLog 实体、Repository、相关路由
  2. **新增 OperationLog 统一日志表**：`log_type` 字段区分 `BUSINESS`（业务日志）和 `SYSTEM`（系统日志）
  3. **AOP 切面日志**：
     - `@BusinessLog`：标注在订单创建/支付/确认/发货/签收/完成/取消等关键方法上，自动记录操作人、目标、详情、耗时
     - `@SystemLog`：用于标注第三方服务调用，记录请求参数、耗时、异常信息
  4. **日志查询与导出**：
     - `GET /api/admins/logs/business` - 分页查询业务日志，支持按模块、操作人、日期范围筛选
     - `GET /api/admins/logs/system` - 分页查询系统日志，支持按模块、结果、日期范围筛选
     - `GET /api/admins/logs/business/export` - 导出业务日志 CSV
     - `GET /api/admins/logs/system/export` - 导出系统日志 CSV
  5. **日志自动清理**：每天凌晨3点执行，删除15天前的日志记录
- 影响范围：
  - `login_log` 表不再使用，由 `operation_log` 表替代
  - `AdminService.authenticateAdmin` 方法签名变更（移除 ipAddress 参数）
- 验证结果：
  - 编译通过

---

## 2026-05-30 (3)
- 修改目的：移除数据备份与恢复相关所有功能（接口、实体、Repository、路由）
- 核心改动：
  - 删除 `BackupRecord.java` 实体和 `BackupRecordRepository.java`
  - `AdminController`：移除 4 个备份相关接口（create/backup、list、restore、delete）
  - `IAdminService`：移除 4 个备份相关方法声明
  - `AdminService`：移除备份相关所有代码及辅助方法（extractDatabaseName、extractHost、extractPort）
  - `SecurityConfig`：移除 `/api/admins/backup/**` 路由
  - 清理不再使用的基础库导入（BufferedReader、File、URI 等）
- 影响范围：
  - 数据备份功能完全移除，仅保留登录日志查看与导出
  - 移除的接口不再可用
- 验证结果：
  - 编译通过

---

## 2026-05-30 (2)
- 修改目的：实现超级管理员端登录日志查看与导出功能
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/PO/LoginLog.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/PO/LoginLog.java)（新增）
  - [`src/main/java/org/dachuang_team/dc_backend_services/repository/LoginLogRepository.java`](./src/main/java/org/dachuang_team/dc_backend_services/repository/LoginLogRepository.java)（新增）
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IAdminService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IAdminService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/AdminService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/AdminService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  1. **登录日志记录**：创建 `login_log` 表实体和 Repository，在管理员登录时自动记录每次登录的详细信息（管理员名称、角色、IP 地址、登录时间、成功/失败状态、失败原因）
  2. **登录日志查看**：
     - `GET /api/admins/logs/login` - 分页查询登录日志，支持按管理员名称、状态、日期范围筛选
     - `GET /api/admins/logs/login/export` - 导出登录日志为 CSV 文件下载
  3. **登录 IP 获取**：登录接口新增 `HttpServletRequest` 参数，通过 `getClientIp()` 方法获取客户端真实 IP（支持代理穿透）
  4. **权限配置**：新增的路由均配置为仅 `SUPER_ADMIN` 角色可访问
- 影响范围：
  - 所有管理员的登录行为都会被记录到 `login_log` 表中
- 验证结果：
  - 编译通过，新增实体和 Repository 自动建表

---

## 2026-05-30
- 修改目的：实现超级管理员管理功能，包括直接创建管理员账号、禁用/启用管理员、重置管理员密码
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/PO/Admin.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/PO/Admin.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/DTO/AdminDTO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/DTO/AdminDTO.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IAdminService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IAdminService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/AdminService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/AdminService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java)
- 核心改动：
  1. **权限调整**：`POST /api/admins/register` 从 `permitAll()` 改为 `hasRole("SUPER_ADMIN")`，仅超级管理员可调用
  2. **Account entity 扩展**：`Admin` 实体类新增 `status` 字段（Integer 类型，0-正常，1-禁用，默认 0）
  3. **注册逻辑改造**：`registerAdmin` 不再依赖邀请码，改为由超级管理员直接指定角色（`ADMIN` / `SUPER_ADMIN`）
  4. **登录拦截**：`authenticateAdmin` 新增账号状态检查，被禁用的管理员无法登录
  5. **新增接口**：
     - `PUT /api/admins/updateStatus` - 超级管理员修改管理员状态（禁用/启用），不允许禁用超级管理员
     - `PUT /api/admins/resetPassword` - 超级管理员重置管理员密码，传入 `adminId` 和 `newPassword`
  6. **列表扩展**：`GET /api/admins/all` 返回值新增 `status` 字段
- 影响范围：
  - 原注册流程的邀请码机制废除，普通管理员不再能通过邀请码自助注册
  - 超级管理员可以通过新接口直接创建任意角色管理员、禁用/启用普通管理员、重置任意管理员密码
  - 被禁用的管理员无法登录系统
- 验证结果：
  - 权限配置已更新，新接口已注册，实体类和DTO已同步

---

## 2026-05-29
- 修改目的：修复商家查询自己所有商品时，商品不存在（无商品）返回404的问题
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java)
- 核心改动：
  - 移除 `getCurrentUserProducts` 方法中 `existsBySellerId` 的检查，当商户暂无商品时返回空列表（200）而非 404
  - 移除 `getApprovedProductsBySeller` 方法中 `existsBySellerId` 的检查，当指定商户暂无商品时返回空列表（200）而非 404
- 影响范围：
  - `GET /api/products/currentMerchant` - 商户查询自己所有商品，无商品时不再返回 404，而是返回空列表
  - `GET /api/products/approvedBySeller` - 根据商户id查询已审核商品，无商品时不再返回 404，而是返回空列表
- 验证结果：
  - 移除多余的存在性检查，让分页查询自然返回空页

---

## 2026-05-25 (2)
- 修改目的：新增住宿外链审核接口和获取所有住宿外链信息接口
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AccommodationController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AccommodationController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IAccommodationService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IAccommodationService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/AccommodationService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/AccommodationService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/AccommodationExternalLinksVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/AccommodationExternalLinksVO.java)（新增）
- 核心改动：
  - 新增接口：`PUT /api/accommodations/approveExternalLink` - 审核通过住宿外链
  - 新增接口：`PUT /api/accommodations/disApproveExternalLink` - 审核不通过住宿外链
  - 新增接口：`GET /api/accommodations/allExternalLinks` - 获取所有住宿对应的外链信息
  - 新增 `AccommodationExternalLinksVO` 类，包含住宿ID、住宿名称、类型、地址和对应的外链列表
  - 在 `IAccommodationService` 中新增方法声明：`ExternalLinkVO approveExternalLink(Long externalLinkId, boolean approved)` 和 `List<AccommodationExternalLinksVO> getAllAccommodationsWithExternalLinks()`
  - 在 `AccommodationService` 中实现上述两个方法
  - 在 `SecurityConfig` 中配置这三个接口为 `ADMIN` 和 `SUPER_ADMIN` 角色可访问
- 影响范围：
  - 管理员和超级管理员可以审核商户添加的住宿外部链接（如携程、飞猪等平台的链接）
  - 管理员和超级管理员可以查看平台所有住宿及其对应的外链信息，返回数据包含住宿ID、名称、类型、地址等关键信息
  - 外链发布后默认未审核（approved=false），需要管理员审核通过后才能在用户端显示
- 验证结果：
  - 新增接口方法和权限配置，SecurityConfig 已更新

## 2026-05-25
- 修改目的：新增住宿管理相关接口，包括获取全部住宿列表和管理员审核住宿功能
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AccommodationController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AccommodationController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IAccommodationService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IAccommodationService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/AccommodationService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/AccommodationService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 新增接口：`GET /api/accommodations/all` - 获取 accommodation 表的所有数据（保留原 `/api/hotels/all` 接口）
  - 新增接口：`PUT /api/accommodations/approve` - 审核通过住宿（将 approved 改为 true）
  - 新增接口：`PUT /api/accommodations/disApprove` - 审核不通过住宿（将 approved 改为 false）
  - 在 `IAccommodationService` 中新增方法声明：`List<Accommodation> getAllAccommodations()` 和 `AccommodationVO approveAccommodation(Long accommodationId, boolean approved)`
  - 在 `AccommodationService` 中实现上述两个方法
  - 在 `SecurityConfig` 中配置这三个接口为 `ADMIN` 和 `SUPER_ADMIN` 角色可访问
- 影响范围：
  - 管理员和超级管理员现在可以查看平台所有住宿数据（通过 `/api/accommodations/all`）
  - 管理员和超级管理员可以审核或拒绝商户发布的住宿
  - 住宿发布后默认未审核（approved=false），需要管理员审核通过后才能在用户端显示
  - 原 `/api/hotels/all` 接口保持不变，两个接口并存
- 验证结果：
  - 新增接口方法和权限配置，SecurityConfig 已更新

## 2026-05-22 (3)
- 修改目的：修复商品更新接口的权限校验逻辑，使其与SecurityConfig配置一致
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java)
- 核心改动：
  - 在 `updateProduct` 方法中将权限校验从 `ROLE_USER` 改为 `ROLE_MERCHANT`
  - 修复了商户调用更新商品接口时返回 "未知权限" 的问题
- 影响范围：
  - 商户现在可以正常更新自己的商品
  - 管理员仍然可以更新所有商品
  - 普通用户不再能调用该接口（与SecurityConfig配置保持一致）
- 验证结果：
  - 权限校验逻辑已修复，与SecurityConfig配置一致

## 2026-05-22 (2)
- 修改目的：在商品列表接口中添加 origin（产地）和 stock（库存）字段
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java)
- 核心改动：
  - 在 `ProductController` 的 `getProductsMapResult` 方法中添加 `origin` 和 `stock` 字段
- 影响范围：
  - 所有返回商品列表的接口现在都会包含 `origin` 和 `stock` 字段（已审核商品、未审核商品、全部商品、商户商品、按卖家查询已审核商品、搜索商品）
  - 商品详情接口原本已有这两个字段，不受影响
- 验证结果：
  - 字段已添加到所有商品列表返回接口中

## 2026-05-22
- 修改目的：在商品列表接口中添加商家id字段，并新增根据id获取商家详细信息的接口
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/MerchantController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/MerchantController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IMerchantService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IMerchantService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/MerchantService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/MerchantService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/MerchantVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/MerchantVO.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 在 `ProductController` 的 `getProductsMapResult` 方法中添加 `sellerId` 字段，影响所有商品列表接口（已审核商品、未审核商品、全部商品、商户商品、按卖家查询已审核商品、搜索商品）
  - 在 `MerchantVO` 类中添加 `id` 字段及其 getter/setter
  - 在 `IMerchantService` 接口中新增方法声明：`MerchantVO getMerchantInfoById(Long merchantId)`
  - 在 `MerchantService` 中实现 `getMerchantInfoById` 方法，根据商户ID查询并返回详细信息
  - 在 `MerchantController` 中新增接口：`GET /api/merchants/info/{merchantId}` - 根据id获取商家基本信息
  - 在 `SecurityConfig` 中配置该接口为已认证用户可访问（`authenticated()`）
- 影响范围：
  - 所有返回商品列表的接口现在都会包含 `sellerId` 字段，前端可以通过该id调用商家详情接口获取商家信息
  - 新增的商家详情接口允许任何已认证用户通过商家id获取商家基本信息（商户姓名、店铺名称、手机号、登录账号、地址、描述、状态等）
  - 商品详情接口原本已有 `sellerId` 字段，不受影响
- 验证结果：
  - 字段已添加到所有商品列表返回接口中
  - 新增接口方法和权限配置，SecurityConfig 已更新

## 2026-05-15
- 修改目的：将获取全部管理员信息列表的接口权限从公开访问改为仅超级管理员可调用
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 将 `/api/admins/all` 接口从 `permitAll()` 改为 `hasRole("SUPER_ADMIN")`
  - 从测试/临时放行接口列表中移除该接口
- 影响范围：
  - 只有 `SUPER_ADMIN` 角色的管理员才能调用该接口
  - 普通管理员和无认证用户将无法访问，会返回 403 权限不足
- 验证结果：
  - SecurityConfig 权限配置已更新

## 2026-05-14 (2)
- 修改目的：添加商户端获取自己基本信息的接口，商户登录后通过token获取自己的详细信息
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IMerchantService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IMerchantService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/MerchantService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/MerchantService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/MerchantController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/MerchantController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 在 `IMerchantService` 接口中新增方法声明：`MerchantVO getMerchantInfo(Long merchantId)`
  - 在 `MerchantService` 中实现 `getMerchantInfo` 方法，根据商户ID查询并返回基本信息
  - 在 `MerchantController` 中新增接口：`GET /api/merchants/info` - 商户获取自己的基本信息
  - 接口无需额外参数，只需在请求头中携带商户的token即可
  - 在 `SecurityConfig` 中配置该接口仅允许 `MERCHANT` 角色访问
- 影响范围：
  - 商户可以通过token获取自己的基本信息（商户姓名、店铺名称、手机号、登录账号、地址、描述、状态等）
  - 完善了商户端的个人信息查询功能
- 验证结果：
  - 新增接口方法和权限配置，SecurityConfig 已更新

## 2026-05-14
- 修改目的：添加管理员端删除系统图片素材的功能，支持通过阿里云OSS删除图片资源
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/ImageController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/ImageController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 在 `ImageController` 中新增接口：`DELETE /api/image/sysImgDelete` - 管理员删除系统图片资源
  - 接口接收 `imageId` 参数，先删除OSS物理文件，再删除数据库记录
  - 在 `SecurityConfig` 中配置该接口仅允许 `ADMIN` 和 `SUPER_ADMIN` 角色访问
  - 权限验证采用双重检查：SecurityConfig 过滤器 + Controller 层角色校验
- 影响范围：
  - 管理员可以通过接口删除不再使用的系统图片素材（如首页Banner等）
  - 删除操作会同时清理OSS存储和数据库记录
- 验证结果：
  - 新增接口方法和权限配置，SecurityConfig 已更新

## 2026-05-10 (2)
- 修改目的：添加管理员端三个统计接口，支持订单统计、产品品类统计和近6个月业务统计
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/repository/OrderRepository.java`](./src/main/java/org/dachuang_team/dc_backend_services/repository/OrderRepository.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/repository/ProductRepository.java`](./src/main/java/org/dachuang_team/dc_backend_services/repository/ProductRepository.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/repository/UserRepository.java`](./src/main/java/org/dachuang_team/dc_backend_services/repository/UserRepository.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/OrderStatsVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/OrderStatsVO.java)（新增）
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/ProductCategoryStatsVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/ProductCategoryStatsVO.java)（新增）
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/SixMonthStatsVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/SixMonthStatsVO.java)（新增）
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/AdminService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/AdminService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 在 `OrderRepository` 中新增 `countByCreatedAtBetween()` 和 `sumTotalAmountByCreatedAtBetween()` 等方法
  - 在 `ProductRepository` 中新增 `countByCategory()` 和 `countByPublishedAtAfter()` 方法
  - 在 `UserRepository` 中新增 `countByCreateTimeAfter()` 方法
  - 新增三个VO类：`OrderStatsVO`、`ProductCategoryStatsVO`、`SixMonthStatsVO`
  - 在 `AdminService` 中新增三个统计方法：`getOrderStatsForPeriods()`、`getProductCategoryStats()`、`getSixMonthStats()`
  - 在 `AdminController` 中新增三个接口：
    - `GET /api/admins/stats/orderStats` - 获取近7天、30天和90天的订单量和销售额
    - `GET /api/admins/stats/productCategoryStats` - 获取产品品类和对应数量
    - `GET /api/admins/stats/sixMonthStats` - 获取近6个月注册用户数、订单数和新商品数
  - 所有接口配置为普通管理员（ADMIN）和超级管理员（SUPER_ADMIN）可访问
- 影响范围：
  - 管理员可以通过统计接口获取平台运营数据
  - 为管理后台数据看板提供数据支持
- 验证结果：
  - 新增接口方法和业务逻辑，权限配置已更新

## 2026-05-10
- 修改目的：添加管理员端获取全部订单信息的接口，方便管理员查看和管理平台所有订单
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/repository/OrderRepository.java`](./src/main/java/org/dachuang_team/dc_backend_services/repository/OrderRepository.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/OrderService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/OrderService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 在 `OrderRepository` 中新增 `findAllByOrderByCreatedAtDesc()` 和 `findByStatus()` 方法
  - 在 `OrderService` 中新增 `getAllOrders()` 方法，支持按订单状态筛选
  - 在 `AdminController` 中新增接口：`GET /api/admins/orders/all` - 获取全部订单列表
  - 接口支持分页查询和按订单状态筛选
  - 配置为普通管理员（ADMIN）和超级管理员（SUPER_ADMIN）可访问
- 影响范围：
  - 管理员可以查看平台所有订单，支持按状态筛选（如待付款、待发货、已完成等）
  - 返回数据包含订单详情、分页信息，与其他订单列表接口保持一致
- 验证结果：
  - 新增接口方法和业务逻辑，权限配置已更新

## 2026-05-09 (2)
- 修改目的：在返回商品数据的接口中添加 approved 字段，方便前端判断商品审核状态
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/ProductController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/FavoritesVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/FavoritesVO.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/ProductService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/ProductService.java)
- 核心改动：
  - 在 `getProductsMapResult` 方法中添加 `approved` 字段，影响所有商品列表接口（已审核商品、未审核商品、全部商品、商户商品、按卖家查询已审核商品、搜索商品）
  - 在 `FavoritesVO` 类中添加 `approved` 字段及其 getter/setter
  - 在 `toFavoritesVO` 方法中通过 productId 查询商品并设置 `approved` 状态
- 影响范围：
  - 所有返回商品列表的接口现在都会包含 `approved` 字段
  - 收藏列表接口也会返回商品的审核状态
  - 商品详情接口原本已有 `approved` 字段，不受影响
- 验证结果：
  - 字段已添加到所有商品列表返回接口中

## 2026-05-09
- 修改目的：添加管理员管理商户的接口，支持获取全部商户信息和修改商户状态（审核/冻结）
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/domain/VO/MerchantAdminVO.java`](./src/main/java/org/dachuang_team/dc_backend_services/domain/VO/MerchantAdminVO.java)（新增）
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/IMerchantService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/IMerchantService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/services/MerchantService.java`](./src/main/java/org/dachuang_team/dc_backend_services/services/MerchantService.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java`](./src/main/java/org/dachuang_team/dc_backend_services/controller/AdminController.java)
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 新增 `MerchantAdminVO` 类，包含商户id、创建时间、描述、登录id、地址、商户姓名、手机号码、账号状态、更新时间和商户名称
  - 新增接口：`GET /api/admins/merchants/all` - 获取全部商户信息列表
  - 新增接口：`PUT /api/admins/merchants/updateStatus` - 修改商户状态（0-待审核，1-审核通过，2-审核不通过，3-已冻结）
  - 两个接口均配置为普通管理员（ADMIN）和超级管理员（SUPER_ADMIN）可访问
  - `MerchantService` 中实现 `getAllMerchants()` 和 `updateMerchantStatusByAdmin()` 方法
- 影响范围：
  - 管理员现在可以查看所有商户信息并进行状态管理
  - 商户注册后默认状态为0（待审核），管理员可通过接口审核通过或冻结
- 验证结果：
  - 新增VO类和接口方法，权限配置已更新

## 2026-04-28
- 修改目的：配合消息中心前端页面接入，避免聊天页相关接口触发未授权拦截导致页面误退出登录。
- 涉及文件：
  - [`src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java`](./src/main/java/org/dachuang_team/dc_backend_services/config/SecurityConfig.java)
- 核心改动：
  - 将 `/api/chat/callBackMessage` 和 `/api/chat/userInfo` 加入认证放行范围。
  - 保持聊天主链路接口统一走已认证逻辑，避免全局 401 拦截误伤聊天页初始化流程。
- 影响范围：
  - 聊天消息撤回接口可正常走认证流程，不再因为未登记而直接被安全配置拦截。
  - 聊天页加载用户头像/昵称时能正常访问用户信息接口。
- 验证结果：
  - 前端消息中心构建通过，后端安全配置已补齐相关接口规则。

## 2026-04-29
- 问题
启动时执行清理任务报错，提示当前线程无可用事务（`No EntityManager with actual transaction available`）。

- 修改位置
`AIInteractionSessionCleanupTask.java` 的 `init()` 方法上增加 `@Transactional` 注解。
