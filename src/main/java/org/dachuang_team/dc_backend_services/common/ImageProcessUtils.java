package org.dachuang_team.dc_backend_services.common;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.UserAvatar;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.ProductImg;
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

    //商品/民宿图片处理方法
    public void processAndCompressImage(ProductImg record, Long productId, int index, boolean isPrimary) {
        try {
            // 处理主图（只有未处理过的才裁剪+压缩+上传）
            if (!Boolean.TRUE.equals(record.getProcessed())) {
                // 从OSS下载原始图
                byte[] rawBytes = ossService.downloadByUrl(record.getUrl());

                // 先读取原始尺寸，进行裁剪成正方形（以短边为基准）
                // 即使前端已经进行了预裁剪，也可能存在尺寸不规范的情况
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawBytes));
                int cropSize = Math.min(original.getWidth(), original.getHeight());

                // 居中裁剪成正方形
                BufferedImage squareImage = Thumbnails.of(original)
                        .sourceRegion(Positions.CENTER, cropSize, cropSize)   // 取最短边居中裁剪
                        .size(cropSize, cropSize)                             // 保持正方形
                        .asBufferedImage();

                // 再缩放到 1000x1000,如果原图已经小于1000x1000则保持原尺寸
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

                // 删除原图
                ossService.delete(record.getUrl());

                // 上传新图
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

    // 用户头像处理方法
    public String userAvatarProcess(UserAvatar record, Long userId){
        try{
            // 用户头像只处理一次，后续如果用户再次上传新头像会覆盖原图并重新处理
            if (!record.isProcessed()) {
                // 从OSS下载原始图
                byte[] rawBytes = ossService.downloadByUrl(record.getAvatarUrl());

                // 先读取原始尺寸，进行裁剪成正方形（以短边为基准）
                BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawBytes));
                int cropSize = Math.min(original.getWidth(), original.getHeight());

                // 居中裁剪成正方形
                BufferedImage squareImage = Thumbnails.of(original)
                        .sourceRegion(Positions.CENTER, cropSize, cropSize)   // 取最短边居中裁剪
                        .size(cropSize, cropSize)                             // 保持正方形
                        .asBufferedImage();

                // 再缩放到 320x320,如果原图已经小于320x320则保持原尺寸
                BufferedImage finalSquare = Thumbnails.of(squareImage)
                        .size(320, 320)
                        .asBufferedImage();

                // 动态压缩到300KB左右
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
                } while (compressedBytes.length > 320 * 320 && quality > 0.1f);

                // 上传图片，更新图片记录信息
                String fileName = String.format("avatars/%d/avatar_%s.jpg",
                        userId, UUID.randomUUID().toString().substring(0, 8));

                // 删除原图
                ossService.delete(record.getAvatarUrl());

                // 上传新图
                String newUrl = ossService.uploadByByte(compressedBytes, fileName);

                record.setAvatarUrl(newUrl);
                record.setProcessed(true);
                record.setLinked(true);

            }

        } catch (IOException e) {
            logger.error("用户头像处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("用户头像处理异常");
        }
        // 无论是否处理过，最终都返回当前记录中的URL（如果之前处理过了，说明之前的URL已经是处理后的图了，可以直接复用）
        return record.getAvatarUrl();
    }
}