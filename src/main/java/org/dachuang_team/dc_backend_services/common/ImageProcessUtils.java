package org.dachuang_team.dc_backend_services.common;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.geometry.Size;
import org.dachuang_team.dc_backend_services.pojo.ProductPO.ProductImageRecord;
import org.dachuang_team.dc_backend_services.services.OssStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Component
public class ImageProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessUtils.class);

    @Autowired
    private OssStorageService ossService;

    public void processAndCompressImage(ProductImageRecord record, Long productId, int index, boolean isPrimary) {
        try {
            // 处理主图（只有未处理过的才裁剪+压缩+上传）
            if (!Boolean.TRUE.equals(record.getProcessed())) {
                // 从OSS下载原始图
                byte[] rawBytes = ossService.downloadByUrl(record.getUrl());

                // 先读取原始尺寸，计算最大正方形裁剪边长
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawBytes));
                int cropSize = Math.min(original.getWidth(), original.getHeight());

                // 居中裁剪成正方形
                BufferedImage squareImage = Thumbnails.of(original)
                        .sourceRegion(Positions.CENTER, cropSize, cropSize)   // 取最短边居中裁剪
                        .size(cropSize, cropSize)                             // 保持正方形
                        .asBufferedImage();

                // 再缩放到 1000x1000
                BufferedImage finalSquare = Thumbnails.of(squareImage)
                        .size(1000, 1000)
                        .asBufferedImage();

                // 动态压缩到 500~1000KB
                float quality = 0.9f;
                byte[] compressedBytes;
                do {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Thumbnails.of(finalSquare)
                            .scale(1.0f)
                            .outputFormat("jpg")
                            .outputQuality(quality)
                            .toOutputStream(baos);
                    compressedBytes = baos.toByteArray();
                    quality -= 0.05f;
                } while (compressedBytes.length > 1024 * 1024 && quality > 0.1f);

                // 上传正式大图
                String fileName = String.format("products/%d/image_%d_%s.jpg",
                        productId, index, UUID.randomUUID().toString().substring(0, 8));
                String newUrl = ossService.uploadByByte(compressedBytes, fileName);

                record.setUrl(newUrl);
                record.setPhysicalPath(fileName);

                // 标记为已处理
                record.setProcessed(true);
                logger.info("压缩裁剪了一张新图片");
            }

            // 处理缩略图（如果是首图且当前没有缩略图才生成，如果有缩略图可以直接复用，不再重复生成）
            if (isPrimary && record.getThumbnailUrl() == null) {
                // 用当前正式大图生成缩略图（不会拉伸）
                byte[] mainBytes = ossService.downloadByUrl(record.getUrl());
                BufferedImage mainImage = ImageIO.read(new ByteArrayInputStream(mainBytes));

                ByteArrayOutputStream thumbOs = new ByteArrayOutputStream();
                Thumbnails.of(mainImage)
                        .size(400, 400)          // 直接缩放到400x400
                        .outputFormat("jpg")
                        .outputQuality(0.8f)
                        .toOutputStream(thumbOs);

                String thumbName = String.format("products/%d/thumbnail_%s.jpg",
                        productId, UUID.randomUUID().toString().substring(0, 8));
                String thumbUrl = ossService.uploadByByte(thumbOs.toByteArray(), thumbName);

                record.setThumbnailUrl(thumbUrl);
                logger.info("生成了一张新缩略图");
            }

            // 统一设置 productId（无论是否处理过主图）
            record.setProductId(productId);

        } catch (IOException e) {
            logger.error("图片压缩处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("图片处理流水线异常");
        }
    }
}