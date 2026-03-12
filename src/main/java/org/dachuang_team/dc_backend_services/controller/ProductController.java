package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Product;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products/all")
    public Result<List<Product>> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return Result.success("获取产品成功", products);
        } catch (Exception e) {
            return Result.error(500, "获取产品失败: " + e.getMessage());
        }
    }

    @PostMapping("/products/add")
    public Result<Product> addProduct(@RequestBody Product product) {
        try {
            if (product.getProductName() == null || product.getProductName().isBlank()) {
                return Result.error(400, "产品名称不能为空");
            }
            if (product.getPrice() < 0) {
                return Result.error(400, "价格不能为负数");
            }
            if (product.getPublishedAt() == null) {
                product.setPublishedAt(LocalDateTime.now());
            }
            Product saved = productRepository.save(product);
            return Result.success("添加成功", saved);
        } catch (Exception e) {
            return Result.error(500, "添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/products/update")
    public Result<Product> updateProduct(@RequestBody Product product) {
        try {
            if (product.getProductId() == null) {
                return Result.error(400, "缺少ID");
            }
            Optional<Product> existing = productRepository.findById(product.getProductId());
            if (existing.isEmpty()) {
                return Result.error(404, "数据不存在");
            }
            if (product.getProductName() == null || product.getProductName().isBlank()) {
                return Result.error(400, "产品名称不能为空");
            }
            if (product.getPrice() < 0) {
                return Result.error(400, "价格不能为负数");
            }
            if (product.getPublishedAt() == null) {
                product.setPublishedAt(existing.get().getPublishedAt());
            }
            Product saved = productRepository.save(product);
            return Result.success("修改成功", saved);
        } catch (Exception e) {
            return Result.error(500, "修改失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/products/delete")
    public Result<Void> deleteProduct(@RequestParam Long id) {
        try {
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
