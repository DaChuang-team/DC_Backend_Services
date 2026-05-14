package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.domain.DTO.ProductDTO;
import org.dachuang_team.dc_backend_services.domain.PO.MerchantPO.Merchant;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ProductImg;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Favorites;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.Product;
import org.dachuang_team.dc_backend_services.domain.VO.FavoritesVO;
import org.dachuang_team.dc_backend_services.repository.FavoritesRepository;
import org.dachuang_team.dc_backend_services.repository.MerchantRepository;
import org.dachuang_team.dc_backend_services.repository.ProductImageRecordRepository;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRecordRepository productImageRecordRepository;

    @Autowired
    private OssStorageService ossService;

    @Autowired
    private ImageProcessUtils imageProcessUtils;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private FavoritesRepository favoritesRepository;

    @Override
    public Product addProduct(ProductDTO productDTO, Long MerchantId) {
        try {
            Merchant seller = merchantRepository.findById(MerchantId).get();
            if(seller == null) {
                throw new IllegalArgumentException("商家不存在");
            }
            if(seller.getStatus() != 1) {
                throw new IllegalStateException("商家未审核通过或被封禁，无法发布商品");
            }

            // 创建商品对象
            Product product = new Product();
            product.setProductName(productDTO.getProductName());
            product.setPrice(productDTO.getPrice());
            product.setCategory(productDTO.getCategory());
            product.setOrigin(productDTO.getOrigin());
            product.setDescription(productDTO.getDescription());
            product.setSeller(seller);
            product.setSellerId(seller.getId());
            product.setStock(productDTO.getStock() != null ? productDTO.getStock() : 0);
            product.setPublishedAt(LocalDateTime.now());
            product.setLastModifiedAt(LocalDateTime.now());
            product.setSales(0);
            product.setApproved(false); // 新增商品默认未审核

            // 先保存商品，拿到 productId
            product = productRepository.save(product);

            // 处理图片列表（如果有）
            if (productDTO.getImageIds() != null && !productDTO.getImageIds().isEmpty()) {
                bindAndProcessImages(product.getProductId(), productDTO.getImageIds());
            }

            return product;
        } catch (Exception e) {
            throw new RuntimeException("添加商品失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public Product updateProductFields(Product existingProduct, ProductDTO productDTO) {
        try {
            // 更新基本字段
            if (productDTO.getProductName() != null && !productDTO.getProductName().isEmpty()) {
                existingProduct.setProductName(productDTO.getProductName());
            }
            if (productDTO.getPrice() != null && productDTO.getPrice() > 0) {
                existingProduct.setPrice(productDTO.getPrice());
            }
            if (productDTO.getCategory() != null && productDTO.getCategory() >= 0) {
                existingProduct.setCategory(productDTO.getCategory());
            }

            if (productDTO.getOrigin() != null && !productDTO.getOrigin().isEmpty()) {
                existingProduct.setOrigin(productDTO.getOrigin());
            }
            if (productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()) {
                existingProduct.setDescription(productDTO.getDescription());
            }
            if (productDTO.getStock() != null && productDTO.getStock() >= 1) {
                existingProduct.setStock(productDTO.getStock());
            }
            existingProduct.setLastModifiedAt(LocalDateTime.now());

            // 处理图片列表（如果前端传了 imageIds）。如果图片有任何更新，都需要按顺序传递完整列表（包括未修改的），否则会被清空
            if (productDTO.getImageIds() != null) {
                // 先解绑所有旧图片，相当于重置状态，等待新列表重新绑定（如果新列表不为空）
                List<ProductImg> oldRecords = productImageRecordRepository.findByProductId(existingProduct.getProductId());
                for (ProductImg rec : oldRecords) {
                    rec.setLinked(false);
                    rec.setProductId(null);
                    // 清空首图标识和排序
                    rec.setPrimary(false);
                    rec.setSortOrder(null);
                    productImageRecordRepository.save(rec);
                }

                // 再绑定新列表（如果新列表不为空）
                if (!productDTO.getImageIds().isEmpty()) {
                    bindAndProcessImages(existingProduct.getProductId(), productDTO.getImageIds());
                } else {
                    // 如果传了空列表，代表清空所有图片（所以不更新就不要传这个字段）。兼容旧字段，清空首图缩略URL
                    existingProduct.setTbImageUrl(null);
                }
            }

            return existingProduct;
        } catch (Exception e) {
            throw new RuntimeException("更新商品失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteProduct(Long productId, Long currentUserId, String currentUserRole) {
        try {
            Product existingProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

            // 权限校验
            if (Objects.equals(currentUserRole, "ROLE_MERCHANT")) {
                if (!existingProduct.getSeller().getId().equals(currentUserId)) {
                    throw new SecurityException("权限不足：您只能删除自己的商品");
                }
            } else if (!Objects.equals(currentUserRole, "ROLE_ADMIN")) {
                throw new SecurityException("权限不足：只有管理员或商家可以删除商品");
            }

            // 解绑并清理所有关联图片记录
            List<ProductImg> records = productImageRecordRepository.findByProductId(productId);
            for (ProductImg rec : records) {
                // 删除OSS文件
                if (rec.getUrl() != null && !rec.getUrl().isEmpty()) {
                    ossService.delete(rec.getUrl());
                }
                if (rec.getThumbnailUrl() != null && !rec.getThumbnailUrl().isEmpty()) {
                    ossService.delete(rec.getThumbnailUrl());
                }
                rec.setLinked(false);
                rec.setProductId(null);
                productImageRecordRepository.save(rec);
            }

            // 删除商品
            productRepository.deleteById(productId);
        } catch (Exception e) {
            System.err.println("删除产品时发生错误: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String favoriteProduct(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new IllegalArgumentException("productId 或 userId 不能为空");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

        if (Boolean.FALSE.equals(product.getApproved())) {
            throw new IllegalStateException("该商品未上架，无法收藏");
        }

        if (favoritesRepository.existsByUserIdAndProductId(userId, productId)) {
            return "该商品已收藏";
        }

        Favorites favorite = new Favorites();
        favorite.setUserId(userId);
        favorite.setProductId(product.getProductId());
        favorite.setProductName(product.getProductName());
        favorite.setTbImageUrl(product.getTbImageUrl());
        favorite.setPrice(BigDecimal.valueOf(product.getPrice()));
        favorite.setSellerId(product.getSellerId());
        favorite.setFavoriteAt(LocalDateTime.now());

        favoritesRepository.save(favorite);
        return "收藏成功";
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String dislikeProduct(Long productId, Long userId) {
        if (productId == null || userId == null) {
            throw new IllegalArgumentException("productId 或 userId 不能为空");
        }

        int affected = favoritesRepository.deleteByUserIdAndProductId(userId, productId);
        if (affected == 0) {
            throw new IllegalArgumentException("收藏记录不存在，无法取消收藏");
        }

        return "取消收藏成功";
    }

    @Override
    public Page<FavoritesVO> getUserFavorites(Long userId, int page, int size) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (page < 1 || size < 1 || size > 100) {
            throw new IllegalArgumentException("非法的页码或页大小");
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Favorites> favoritesPage = favoritesRepository.findByUserId(userId, pageable);

        return favoritesPage.map(this::toFavoritesVO);
    }

    private FavoritesVO toFavoritesVO(Favorites favorite) {
        FavoritesVO vo = new FavoritesVO();
        vo.setProductId(favorite.getProductId());
        vo.setProductName(favorite.getProductName());
        vo.setTbImageUrl(favorite.getTbImageUrl());
        vo.setPrice(favorite.getPrice());
        vo.setSellerId(favorite.getSellerId());
        vo.setFavoriteAt(favorite.getFavoriteAt());

        Product product = productRepository.findById(favorite.getProductId()).orElse(null);
        if (product != null) {
            vo.setApproved(product.getApproved());
        }

        return vo;
    }


    // 绑定 + 处理图片（压缩、裁剪、生成缩略图）
    private void bindAndProcessImages(Long productId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) return;
        if (imageIds.size() > 5) throw new IllegalArgumentException("商品最多支持5张图片");


        // 按照前端传递的顺序绑定图片，并且第0位为首图（primary），后续位为非首图。
        // 按照processed字段和tumbnailUrl字段来判断是否需要处理（如果之前已经处理过了，就不重复处理了）。如果之前是未处理状态，或者之前是非首图现在变成首图了，都需要重新处理生成主图或缩略图
        // 具体实现在ImageProcessUtils里
        for (int i = 0; i < imageIds.size(); i++) {
            Long recordId = imageIds.get(i);
            ProductImg record = productImageRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("图片记录不存在: " + recordId));

            boolean isPrimary = (i == 0);

            if (!Boolean.TRUE.equals(record.getLinked())) {
                imageProcessUtils.productImgProcessAndCompress(record, productId, i, isPrimary);
            }

            // 更新绑定状态
            record.setLinked(true);
            record.setProductId(productId);
            record.setSortOrder(i);
            record.setPrimary(isPrimary);

            productImageRecordRepository.save(record);
        }

        // 同步更新商品主表（首图冗余）
        syncProductMainTbImage(productId, imageIds.get(0));
    }

    private void syncProductMainTbImage(Long productId, Long mainImageRecordId) {
        ProductImg mainRecord = productImageRecordRepository.findById(mainImageRecordId).get();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品未找到"));

        // 首图冗余字段更新为当前首图的缩略图URL，此字段仅用加快前端列表展示。
        product.setTbImageUrl(mainRecord.getThumbnailUrl());
        productRepository.save(product);
    }
}
