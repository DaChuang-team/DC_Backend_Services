package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.ProductDTO;
import org.dachuang_team.dc_backend_services.pojo.Product;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.dachuang_team.dc_backend_services.services.ProductService;
import org.dachuang_team.dc_backend_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    //返回所有已经审核通过的产品，分页返回
    @GetMapping("/products/approved")
    public Result<Map<String, Object>> getApprovedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = getCurrentUserId();
            Pageable pageable = validateAndPreparePageable(page, size);
            Page<Product> productPage = productRepository.findByApprovedTrue(pageable);

            return getProductsMapResult(productPage, currentUserId);
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
            Pageable pageable = validateAndPreparePageable(page, size);
            Page<Product> productPage = productRepository.findByApprovedFalse(pageable);

            // 构建分页响应数据
            return getProductsMapResult(productPage, currentUserId);
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
            Pageable pageable = validateAndPreparePageable(page, size);
            Page<Product> productPage = productRepository.findAll(pageable);

            return getProductsMapResult(productPage, currentUserId);
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

            // 根据userId查询User_General实例
            User_General seller = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

            Pageable pageable = validateAndPreparePageable(page, size);

            // 查询与该User_General关联的产品
            Page<Product> productPage = productRepository.findBySeller(seller, pageable);

            return getProductsMapResult(productPage, currentUserId);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "获取产品失败: " + e.getMessage());
        }
    }

    private Result<Map<String, Object>> getProductsMapResult(Page<Product> productPage, long sellerId) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> filteredProducts = productPage.getContent().stream().map(product -> {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productId", product.getProductId());
            productMap.put("productName", product.getProductName());
            productMap.put("price", product.getPrice());
            productMap.put("category", product.getCategory());
            productMap.put("origin", product.getOrigin());
            productMap.put("approved", product.getApproved());
            productMap.put("publishedAt", product.getPublishedAt());
            productMap.put("imageUrl", product.getImageUrl());
            productMap.put("sellerId", sellerId);
            productMap.put("description", product.getDescription());

            return productMap;
        }).toList();

        response.put("products", filteredProducts);
        response.put("currentPage", productPage.getNumber() + 1); // 返回给前端的页码从1开始
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());

        return Result.success("获取产品成功", response);
    }

    private Pageable validateAndPreparePageable(int page, int size) {
        // 验证页码和大小参数
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("非法的页码和页大小");
        }

        // 将页码从1开始调整为从0开始
        int adjustedPage = Math.max(page - 1, 0);
        return PageRequest.of(adjustedPage, size);
    }

    private Long getCurrentUserId() {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUserId == null) {
            throw new SecurityException("未认证");
        }
        return currentUserId;
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
            Product newProduct =  productService.addProduct(productDTO, currentUserId);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("productName", newProduct.getProductName());
            responseBody.put("price", newProduct.getPrice());
            responseBody.put("category", newProduct.getCategory());
            responseBody.put("origin", newProduct.getOrigin());

            return Result.success("添加成功", responseBody);
        } catch (Exception e) {
            return Result.error(500, "添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/products/update")
    public Result<Product> updateProduct(@RequestBody Product productDTO) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证");
            }
            if (productDTO.getProductId() == null) {
                return Result.error(400, "缺少ID");
            }
            Optional<Product> existing = productRepository.findById(productDTO.getProductId());
            if (existing.isEmpty()) {
                return Result.error(404, "数据不存在");
            }
            if (productDTO.getProductName() == null || productDTO.getProductName().isBlank()) {
                return Result.error(400, "产品名称不能为空");
            }
            if (productDTO.getPrice() < 0) {
                return Result.error(400, "价格不能为负数");
            }
            if (productDTO.getPublishedAt() == null) {
                productDTO.setPublishedAt(existing.get().getPublishedAt());
            }
            Product saved = productRepository.save(productDTO);
            return Result.success("修改成功", saved);
        } catch (Exception e) {
            return Result.error(500, "修改失败: " + e.getMessage());
        }
    }

    @PostMapping("/products/approve")
    public Result<Product> approveProduct(@RequestParam Long Pid) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证");
            }
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

    @PostMapping("/products/disApprove")
    public Result<Product> disApproveProduct(@RequestParam Long Pid) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证");
            }
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
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUserId == null) {
                return Result.error(401, "未认证");
            }
            Optional<Product> existing = productRepository.findByproductId(Pid);
            return existing.map(product -> {
                Map<String, Object> productDetails = new HashMap<>();
                productDetails.put("productId", product.getProductId());
                productDetails.put("productName", product.getProductName());
                productDetails.put("price", product.getPrice());
                productDetails.put("category", product.getCategory());
                productDetails.put("origin", product.getOrigin());
                productDetails.put("approved", product.getApproved());
                productDetails.put("publishedAt", product.getPublishedAt());
                productDetails.put("imageUrl", product.getImageUrl());
                productDetails.put("description", product.getDescription());
                productDetails.put("sellerId", currentUserId);

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
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证");
            }
            Pageable pageable = validateAndPreparePageable(page, size);
            Page<Product> productPage = productRepository.findByProductNameContainingIgnoreCase(keyword, pageable);

            return getProductsMapResult(productPage, currentUserId);
        } catch (Exception e) {
            return Result.error(500, "搜索商品失败: " + e.getMessage());
        }
    }
    

    @DeleteMapping("/products/delete")
    public Result<Void> deleteProduct(@RequestParam Long id) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(currentUserId == null) {
                return Result.error(401, "未认证");
            }
            if (!productRepository.existsById(id)) {
                return Result.error(404, "数据不存在");
            }
            productRepository.deleteById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(500, "删除失败: " + e.getMessage());
        }
    }
}
