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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageProcessUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessUtils.class);

    @Autowired
    private OssStorageService ossService;

    public void processAndCompressImage(ProductImageRecord record, Long productId, int index, boolean isPrimary) {
        try {
            // 从OSS下载原始图
            byte[] rawBytes = ossService.downloadByUrl(record.getUrl());

            // 中心裁剪并调整尺寸为 1000x1000 (正方形)
            // Thumbnailator会自动根据Positions.CENTER裁掉多余的边
            BufferedImage squareImage = Thumbnails.of(new ByteArrayInputStream(rawBytes))
                    .sourceRegion(Positions.CENTER, 1000, 1000)
                    .size(1000, 1000)
                    .keepAspectRatio(false)
                    .asBufferedImage();

            // 动态压缩逻辑：确保文件大小在500KB~1000KB之间
            float quality = 0.9f;
            byte[] compressedBytes;
            do {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Thumbnails.of(squareImage)
                        .scale(1.0f)
                        .outputFormat("jpg")
                        .outputQuality(quality)
                        .toOutputStream(baos);
                compressedBytes = baos.toByteArray();

                // 如果文件还是太大（超过 1000KB），则降低 5% 质量继续尝试
                quality -= 0.05f;
            } while (compressedBytes.length > 1024 * 1024 && quality > 0.1f);

            // 上传处理后的正式图
            String fileName = String.format("products/%d/image_%d_%s.jpg",
                    productId, index, java.util.UUID.randomUUID().toString().substring(0, 8));
            String newUrl = ossService.uploadByByte(compressedBytes, fileName);

            record.setUrl(newUrl);
            record.setPhysicalPath(fileName);

            // 如果是首图则追加生成 400x400 缩略图
            if (isPrimary) {
                ByteArrayOutputStream thumbOs = new ByteArrayOutputStream();
                Thumbnails.of(squareImage)
                        .size(400, 400)
                        .outputFormat("jpg")
                        .outputQuality(0.8f)
                        .toOutputStream(thumbOs);

                String thumbName = "products/" + productId + "/thumbnail.jpg";
                String thumbUrl = ossService.uploadByByte(compressedBytes, thumbName);
                record.setThumbnailUrl(thumbUrl);
            }

            record.setProductId(productId);

        } catch (IOException e) {
            logger.error("图片压缩处理失败: {}", e.getMessage());
            throw new RuntimeException("图片处理流水线异常");
        }
    }
}