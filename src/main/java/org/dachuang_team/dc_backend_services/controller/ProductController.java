package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.ProductDTO;
import org.dachuang_team.dc_backend_services.pojo.Product;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/products/all")
    public Result<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (currentUserId == null) {
                return Result.error(401, "未认证");
            }

            // 验证页码和大小参数
            if (page < 1 || size < 1) {
                return Result.error(400, "非法的页码和页大小");
            }

            // 将页码从1开始调整为从0开始
            int adjustedPage = Math.max(page - 1, 0);
            Pageable pageable = PageRequest.of(adjustedPage, size);
            Page<Product> productPage = productRepository.findAll(pageable);

            // 构建分页响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("products", productPage.getContent());
            response.put("currentPage", productPage.getNumber() + 1); // 返回给前端的页码从1开始
            response.put("totalItems", productPage.getTotalElements());
            response.put("totalPages", productPage.getTotalPages());

            return Result.success("获取产品成功", response);
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
