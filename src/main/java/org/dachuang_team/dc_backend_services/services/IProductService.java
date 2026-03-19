package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.ProductDTO;
import org.dachuang_team.dc_backend_services.pojo.ProductPO.Product;

public interface IProductService {
    Product addProduct(ProductDTO product, Long userId);
    Product updateProductFields(Product existingProduct, ProductDTO productDTO);
    void deleteProduct(Long productId, Long currentUserId, String currentUserRole);
}
