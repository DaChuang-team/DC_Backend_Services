package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.ProductDTO;
import org.dachuang_team.dc_backend_services.pojo.Product;

public interface IProductService {
    Product addProduct(ProductDTO product, Long userId);
}
