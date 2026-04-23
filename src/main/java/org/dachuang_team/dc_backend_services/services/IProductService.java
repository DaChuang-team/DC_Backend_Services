package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.ProductDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Favorites;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.dachuang_team.dc_backend_services.domain.VO.FavoritesVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {
    Product addProduct(ProductDTO product, Long userId);
    Product updateProductFields(Product existingProduct, ProductDTO productDTO);
    void deleteProduct(Long productId, Long currentUserId, String currentUserRole);
    String favoriteProduct(Long productId, Long currentUserId);
    String dislikeProduct(Long productId, Long currentUserId);
    Page<FavoritesVO> getUserFavorites(Long userId, int page, int size);
}
