package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.ProductDTO;
import org.dachuang_team.dc_backend_services.pojo.productImageRecord;
import org.dachuang_team.dc_backend_services.pojo.Product;
import org.dachuang_team.dc_backend_services.pojo.userGeneral;
import org.dachuang_team.dc_backend_services.repository.productImageRecordRepository;
import org.dachuang_team.dc_backend_services.repository.productRepository;
import org.dachuang_team.dc_backend_services.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService implements IProductService {

    @Autowired
    private productRepository productRepository;

    @Autowired
    private userRepository userRepository;

    @Autowired
    private productImageRecordRepository productImageRecordRepository;

    @Override
    public Product addProduct(ProductDTO productDTO, Long userId) {
        try {
            userGeneral seller = userRepository.findById(userId).orElseThrow(()
                    -> new IllegalArgumentException("用户ID: " + userId + " 不存在"));

            // 创建商品对象
            Product product = new Product();
            product.setProductName(productDTO.getProductName());
            product.setPrice(productDTO.getPrice());
            product.setCategory(productDTO.getCategory());
            product.setOrigin(productDTO.getOrigin());
            product.setDescription(productDTO.getDescription());
            product.setseller(seller);
            product.setImageUrl(productDTO.getImgUrl());
            product.setPublishedAt(LocalDateTime.now());
            product.setLastModifiedAt(LocalDateTime.now());

            // 保存商品以生成ID
            product = productRepository.save(product);

            // 如果图片URL不为空，尝试关联图片记录
            if (productDTO.getImgUrl() != null && !productDTO.getImgUrl().isEmpty()) {
                Optional<productImageRecord> record = productImageRecordRepository.findByUrl(productDTO.getImgUrl());
                if (record.isPresent()) {
                    productImageRecord productImageRecord = record.get();
                    productImageRecord.setLinked(true);
                    productImageRecord.setProductId(product.getProductId()); // 绑定商品ID
                    productImageRecordRepository.save(productImageRecord);
                } else {
                    System.err.println("未找到图片记录，URL: " + productDTO.getImgUrl());
                }
            }

            return product;
        } catch (Exception e) {
            System.err.println("添加产品时发生错误: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Product updateProductFields(Product existingProduct, ProductDTO productDTO) {
        if (productDTO.getImgUrl() != null) { // 前端返回了imgUrl字段
            if (productDTO.getImgUrl().isEmpty()) { // imgUrl为空字符串，解绑旧图片记录
                String oldImageUrl = existingProduct.getImageUrl();
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    Optional<productImageRecord> oldRecord = productImageRecordRepository.findByUrl(oldImageUrl);
                    oldRecord.ifPresent(productImageRecord -> {
                        productImageRecord.setLinked(false);
                        productImageRecordRepository.save(productImageRecord);
                    });
                }
                existingProduct.setImageUrl(null); // 清空图片 URL
            } else { // imgUrl不为空，绑定新图片记录
                String oldImageUrl = existingProduct.getImageUrl();
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    Optional<productImageRecord> oldRecord = productImageRecordRepository.findByUrl(oldImageUrl);
                    oldRecord.ifPresent(productImageRecord -> {
                        productImageRecord.setLinked(false);
                        productImageRecordRepository.save(productImageRecord);
                    });
                }
                Optional<productImageRecord> record = productImageRecordRepository.findByUrl(productDTO.getImgUrl());
                if (record.isPresent()) {
                    productImageRecord productImageRecord = record.get();
                    productImageRecord.setLinked(true);
                    productImageRecord.setProductId(existingProduct.getProductId());
                    productImageRecordRepository.save(productImageRecord);
                    existingProduct.setImageUrl(productDTO.getImgUrl());
                } else {
                    System.err.println("未找到图片记录，URL: " + productDTO.getImgUrl());
                }
            }
        }

        if (productDTO.getProductName() != null && !productDTO.getProductName().isEmpty()) {
            existingProduct.setProductName(productDTO.getProductName());
        }
        if (productDTO.getPrice() >= 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }
        if (productDTO.getCategory() > 0) {
            existingProduct.setCategory(productDTO.getCategory());
        }
        if (productDTO.getOrigin() != null && !productDTO.getOrigin().isEmpty()) {
            existingProduct.setOrigin(productDTO.getOrigin());
        }
        if (productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()) {
            existingProduct.setDescription(productDTO.getDescription());
        }
        existingProduct.setLastModifiedAt(LocalDateTime.now());

        return existingProduct;
    }

    @Override
    public void deleteProduct(Long productId, Long currentUserId, String currentUserRole) {
        // 查询商品是否存在
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

        // 权限校验，用户只能删除自己的商品，管理员可以删除所有商品
        if (Objects.equals(currentUserRole, "ROLE_USER")) {
            if (!existingProduct.getseller().getUserId().equals(currentUserId)) {
                throw new SecurityException("权限不足：您只能删除自己的商品");
            }
        } else if (!Objects.equals(currentUserRole, "ROLE_ADMIN")) {
            throw new SecurityException("未知权限");
        }

        // 如果商品的图片URL不为空，解绑图片记录
        String imageUrl = existingProduct.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Optional<productImageRecord> record = productImageRecordRepository.findByUrl(imageUrl);
            record.ifPresent(productImageRecord -> {
                productImageRecord.setLinked(false); // 解绑图片记录
                productImageRecordRepository.save(productImageRecord);
            });
        }

        // 删除商品
        productRepository.deleteById(productId);
    }
}
