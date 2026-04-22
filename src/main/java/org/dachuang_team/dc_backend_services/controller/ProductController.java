package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.DTO.ProductDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ProductImg;
import org.dachuang_team.dc_backend_services.repository.ProductImageRecordRepository;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageRecordRepository productImageRecordRepository;

    //返回所有已经审核通过的产品，分页返回
    @GetMapping("/products/approved")
    public Result<Map<String, Object>> getApprovedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            long totalItems = productRepository.countByApprovedTrue(); // 获取总记录数
            Pageable pageable = validateAndPreparePageable(page, size, totalItems);
            Page<Product> productPage = productRepository.findByApprovedTrue(pageable);

            return getProductsMapResult(productPage);
        } catch (Exception e) {
            return Result.error(500, "获取产品失败: " + e.getMessage());
        }
    }

    //返回所有未审核的产品，分页返回
    @GetMapping("/products/unApproved")
    public Result<Map<String, Object>> getUnapprovedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
        try {
            Long currentUserId = getCurrentUserId();
            long totalItems = productRepository.countByApprovedFalse(); // 获取总记录数
            Pageable pageable = validateAndPreparePageable(page, size, totalItems);
            Page<Product> productPage = productRepository.findByApprovedFalse(pageable);

            // 构建分页响应数据
            return getProductsMapResult(productPage);
        } catch (Exception e) {
            return Result.error(500, "获取产品失败: " + e.getMessage());
        }
    }

    //返回所有产品，分页返回
    @GetMapping("/products/all")
    public Result<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
        try {
            Long currentUserId = getCurrentUserId();
            long totalItems = productRepository.count(); // 获取总记录数
            Pageable pageable = validateAndPreparePageable(page, size, totalItems);
            Page<Product> productPage = productRepository.findAll(pageable);

            return getProductsMapResult(productPage);
        } catch (Exception e) {
            return Result.error(500, "获取产品失败: " + e.getMessage());
        }
    }

    //返回当前用户的所有产品，分页返回
    @GetMapping("/products/currentUser")
    public Result<Map<String, Object>> getCurrentUserProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = getCurrentUserId();

            long totalItems = productRepository.countBySellerId(currentUserId);
            Pageable pageable = validateAndPreparePageable(page, size, totalItems);
            Page<Product> productPage = productRepository.findBySellerId(currentUserId, pageable);

            return getProductsMapResult(productPage);
        } catch (Exception e) {
            return Result.error(500, "获取产品失败: " + e.getMessage());
        }
    }



    @PostMapping("/products/add")
    public Result<Map<String, Object>> addProduct(@RequestBody ProductDTO productDTO) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUserId == null) {
                return Result.error(401, "未认证");
            }
            if (productDTO.getProductName() == null || productDTO.getProductName().isEmpty()) {
                return Result.error(400, "产品名称不能为空");
            }
            if (productDTO.getPrice() < 0) {
                return Result.error(400, "价格不能为负数");
            }
            if(productDTO.getStock() < 0){
                return Result.error(400, "库存不能为负数");
            }
            Product newProduct =  productService.addProduct(productDTO, currentUserId);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("productName", newProduct.getProductName());
            responseBody.put("price", newProduct.getPrice());
            responseBody.put("category", newProduct.getCategory());
            responseBody.put("origin", newProduct.getOrigin());
            responseBody.put("description", newProduct.getDescription());
            responseBody.put("productId", newProduct.getProductId());
            responseBody.put("TbImageUrl", newProduct.getTbImageUrl()); // 缩略图URL
            responseBody.put("stock", newProduct.getStock());

            return Result.success("添加成功", responseBody);
        } catch (Exception e) {
            return Result.error(500, "添加失败: " + e.getMessage());
        }
    }

    // 更新商品信息，传入一个包含商品id和需要更新的字段的DTO对象，返回更新后的商品信息
    @PutMapping("/products/update")
    public Result<Map<String, Object>> updateProduct(
            @RequestBody ProductDTO productDTO,
            @RequestParam Long Pid) {
        try {
            Long currentUserId = getCurrentUserId();
            String currentUserRole = getCurrentUserRole();
            // 查询商品是否存在
            Product existingProduct = productRepository.findById(Pid)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
            // 权限校验,用户只能更新自己的商品，管理员可以更新所有商品
            if (Objects.equals(currentUserRole, "ROLE_USER")) {
                if (!existingProduct.getSeller().getId().equals(currentUserId)) {
                    return Result.error(403, "权限不足：您只能更新自己的商品");
                }
            } else if (!Objects.equals(currentUserRole, "ROLE_ADMIN")) {
                return Result.error(401, "未知权限");
            }
            // 调用服务层更新商品信息
            Product updatedProduct = productService.updateProductFields(existingProduct, productDTO);
            // 保存更新后的商品
            Product savedProduct = productRepository.save(updatedProduct);
            // 构建返回体
            Map<String, Object> productDetails = getProductDetails(savedProduct);

            return Result.success("商品更新成功", productDetails);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "更新商品失败: " + e.getMessage());
        }
    }


    // 管理员接口，使编号为id的商品审核通过
    @PostMapping("/products/approve")
    public Result<Product> approveProduct(@RequestParam Long Pid) {
        try {
            Long currentUserId = getCurrentUserId();
            Optional<Product> existing = productRepository.findByproductId(Pid);
            if (existing.isEmpty()) {
                return Result.error(404, "数据不存在");
            }
            Product product = existing.get();
            product.setApproved(true);
            Product saved = productRepository.save(product);
            return Result.success("商品已审核", null);
        } catch (Exception e) {
            return Result.error(500, "审核状态设置失败: " + e.getMessage());
        }
    }

    // 管理员接口，封禁）编号为id的商品
    @PostMapping("/products/disApprove")
    public Result<Product> disApproveProduct(@RequestParam Long Pid) {
        try {
            Long currentUserId = getCurrentUserId();
            Optional<Product> existing = productRepository.findByproductId(Pid);
            if (existing.isEmpty()) {
                return Result.error(404, "数据不存在");
            }
            Product product = existing.get();
            product.setApproved(false);
            Product saved = productRepository.save(product);
            return Result.success("商品已封禁", null);
        } catch (Exception e) {
            return Result.error(500, "审核状态设置失败: " + e.getMessage());
        }
    }


    //通过传入商品id返回商品的详细信息
    @GetMapping("/products/details")
    public Result<Map<String, Object>> getProductDetailsById(@RequestParam Long Pid) {
        try {
            Optional<Product> existing = productRepository.findByproductId(Pid);
            return existing.map(product -> {
                Map<String, Object> productDetails = getProductDetails(product);

                return Result.success("获取商品详情成功", productDetails);
            }).orElseGet(() -> Result.error(404, "数据不存在"));
        } catch (Exception e) {
            return Result.error(500, "获取商品详情失败: " + e.getMessage());
        }
    }

    //通过商品名称模糊搜索商品，分页返回
    @GetMapping("/products/search")
    public Result<Map<String, Object>> searchProductByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // 获取符合条件的总记录数
            long totalItems = productRepository.countByProductNameContainingIgnoreCase(keyword);

            Pageable pageable = validateAndPreparePageable(page, size, totalItems);

            Page<Product> productPage = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

            return getProductsMapResult(productPage);
        } catch (Exception e) {
            return Result.error(500, "搜索商品失败: " + e.getMessage());
        }
    }


    @DeleteMapping("/products/delete")
    public Result<Void> deleteProduct(@RequestParam Long Pid) {
        try {
            Long currentUserId = getCurrentUserId();
            String currentUserRole = getCurrentUserRole();

            productService.deleteProduct(Pid, currentUserId, currentUserRole);

            return Result.success("删除成功", null);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUserId == null) {
            throw new SecurityException("未认证");
        }
        return currentUserId;
    }

    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
    }

    //分页查询时只返回产品的部分信息，需要使用/products/details来获取完整信息
    private Result<Map<String, Object>> getProductsMapResult(Page<Product> productPage) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> filteredProducts = productPage.getContent().stream().map(product -> {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productId", product.getProductId());
            productMap.put("productName", product.getProductName());
            productMap.put("price", product.getPrice());
            productMap.put("category", product.getCategory());
            productMap.put("TbImageUrl", product.getTbImageUrl()); // 首图缩略图URL

            return productMap;
        }).toList();

        response.put("products", filteredProducts);
        response.put("currentPage", productPage.getNumber() + 1); // 返回给前端的页码从1开始
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());

        return Result.success("获取产品成功", response);
    }

    // 构建单个商品的详细信息返回体
    private Map<String, Object> getProductDetails(Product savedProduct) {

        Map<String, Object> productDetails = new HashMap<>();

        productDetails.put("productId", savedProduct.getProductId());
        productDetails.put("productName", savedProduct.getProductName());
        productDetails.put("price", savedProduct.getPrice());
        productDetails.put("category", savedProduct.getCategory());
        productDetails.put("origin", savedProduct.getOrigin());
        productDetails.put("approved", savedProduct.getApproved());
        productDetails.put("publishedAt", savedProduct.getPublishedAt());
        productDetails.put("TbImageUrl", savedProduct.getTbImageUrl());
        productDetails.put("lastModifiedAt", savedProduct.getLastModifiedAt());
        productDetails.put("description", savedProduct.getDescription());
        productDetails.put("stock", savedProduct.getStock());
        productDetails.put("sellerId", savedProduct.getSellerId());

        // 获取productId
        Long productId = savedProduct.getProductId();

        // 按sortOrder排序查询
        List<ProductImg> images =
                productImageRecordRepository.findByProductIdOrderBySortOrderAsc(productId);

        // 组装返回（id + order + url）
        List<Map<String, Object>> imageList = images.stream().map(img -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", img.getId());
            map.put("order", img.getSortOrder());
            map.put("url", img.getUrl());
            return map;
        }).collect(Collectors.toList());

        productDetails.put("images", imageList);

        return productDetails;
    }

    private Pageable validateAndPreparePageable(int page, int size, long totalItems) {
        // 验证页码和大小参数
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("非法的页码和页大小");
        }

        // 计算总页数
        int totalPages = (int) Math.ceil((double) totalItems / size);

        // 如果页码大于总页数，调整为最后一页
        int adjustedPage = Math.min(page - 1, Math.max(totalPages - 1, 0));

        return PageRequest.of(adjustedPage, size);
    }
}
