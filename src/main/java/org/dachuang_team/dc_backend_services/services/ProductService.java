package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.ProductDTO;
import org.dachuang_team.dc_backend_services.pojo.Product;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public Product addProduct(ProductDTO productDTO, Long userId) {
        try {
            User_General seller = userRepository.findById(userId).orElseThrow(()
                    -> new IllegalArgumentException("用户ID: " + userId + " 不存在"));

            Product product = new Product();
            product.setProductName(productDTO.getProductName());
            product.setPrice(productDTO.getPrice());
            product.setCategory(productDTO.getCategory());
            product.setOrigin(productDTO.getOrigin());
            product.setseller(seller);
            product.setPublishedAt(LocalDateTime.now());
            product.setLastModifiedAt(LocalDateTime.now());

            productRepository.save(product);
            return product;
        } catch (Exception e) {
            System.err.println("添加产品时发生错误: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Product updateProductFields(Product existingProduct, ProductDTO productDTO) {
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
}
