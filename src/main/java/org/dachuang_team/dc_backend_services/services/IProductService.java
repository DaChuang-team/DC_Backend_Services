package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.ProductDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;

public interface IProductService {
    Product addProduct(ProductDTO product, Long userId);
    Product updateProductFields(Product existingProduct, ProductDTO productDTO);
    void deleteProduct(Long productId, Long currentUserId, String currentUserRole);
}
