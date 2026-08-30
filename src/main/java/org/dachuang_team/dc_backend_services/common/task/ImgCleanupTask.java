package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.*;
import org.dachuang_team.dc_backend_services.repository.*;
import org.dachuang_team.dc_backend_services.services.IStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ImgCleanupTask {

    @Autowired
    private ProductImageRecordRepository productImageRecordRepository; // 商品图片记录

    @Autowired
    private UserAvatarRecordRepository userAvatarRecordRepository; // 用户头像记录

    @Autowired
    private AIInteractionImgRepository aiInteractionImgRepository; // AI交互图片记录

    @Autowired
    private ShopBannerImgRepository shopBannerImgRepository; // 店铺首页横幅图片记录

    @Autowired
    private RefundImgRepository refundImgRepository; // 退款图片记录
    
    @Autowired
    private AccommodationImgRepository accommodationImgRepository; // 酒店图片记录

    @Autowired
    private IStorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(ImgCleanupTask.class);

    // 程序完全启动就绪后执行一次
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        cleanOldUnlinkedImages();
    }

    // 每天凌晨一点执行一次
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldUnlinkedImages() {
        logger.info("开始执行业务冗余图片清理任务");
        int deletedCount = 0;
        try {
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

            // 查询符合条件的记录
            List<ProductImg> PdImgsToDelete =
                    productImageRecordRepository.findAllByCreatedAtBeforeAndIsLinkedFalse(threeDaysAgo);

            List<UserAvatar> avatarImgsToDelete =
                    userAvatarRecordRepository.findAllByUploadAtBeforeAndIsLinkedFalse(threeDaysAgo);

            List<AIInteractionImg> AIImgsToDelete =
                    aiInteractionImgRepository.findAllByUploadTimeBefore(threeDaysAgo);

            List<ShopBannerImg> shopBannerImgsToDelete =
                    shopBannerImgRepository.findAllByUploadTimeBeforeAndIsLinkedFalse(threeDaysAgo);

            List<RefundImg> refundImgsToDelete =
                    refundImgRepository.findAllByUploadTimeBeforeAndIsLinkedFalse(threeDaysAgo);
            
            List<AccommodationImg> accommodationImgsToDelete =
                    accommodationImgRepository.findAllByIsLinkedFalseAndCreatedAtBefore(threeDaysAgo);
                    

            if (PdImgsToDelete.isEmpty() &&
                    avatarImgsToDelete.isEmpty() &&
                    AIImgsToDelete.isEmpty() &&
                    shopBannerImgsToDelete.isEmpty() &&
                    refundImgsToDelete.isEmpty() &&
                    accommodationImgsToDelete.isEmpty()){
                logger.info("没有需要清理的图片资源");
                return;
            }

            // 循环处理删除商品图片记录
            for (ProductImg record : PdImgsToDelete) {
                String url = record.getUrl();
                if(record.getThumbnailUrl() != null) {
                    // 先删云端缩略图
                    try {
                        storageService.delete(record.getThumbnailUrl());
                    } catch (Exception e) {
                        logger.warn("删除商品冗余图片缩略图失败 URL: {} 错误: {}", record.getThumbnailUrl(), e.getMessage(), e);
                    }
                }
                try {
                    // 先删云端
                    storageService.delete(url);
                    // 再删数据库记录
                    productImageRecordRepository.deleteByUrl(url);

                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("删除商品冗余图片失败 URL: {} 错误: {}", url, e.getMessage(), e);
                }
            }

            // 循环处理删除用户头像记录
            for (UserAvatar record : avatarImgsToDelete) {
                String url = record.getAvatarUrl();
                try {
                    // 先删云端
                    storageService.delete(url);
                    // 再删数据库记录
                    userAvatarRecordRepository.deleteByAvatarUrl(url);

                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("删除用户冗余头像图片失败 URL: {} 错误: {}", url, e.getMessage(), e);
                }
            }

            // 循环处理删除AI交互缓存图片记录
            for (AIInteractionImg img : AIImgsToDelete) {
                String url = img.getImageUrl();
                try {
                    // 先删云端
                    storageService.delete(url);
                    // 再删数据库记录
                    aiInteractionImgRepository.deleteByImageUrl(url);

                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("删除AI交互缓存图片失败 URL: {} 错误: {}", url, e.getMessage(), e);
                }
            }
            
            // 循环处理商家首页横幅图片记录
            for(ShopBannerImg img : shopBannerImgsToDelete) {
                String url = img.getImgUrl();
                try {
                    storageService.delete(url);
                    shopBannerImgRepository.deleteByImgUrl(url);
                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("删除店铺横幅冗余图片失败 URL: {} 错误: {}", url, e.getMessage(), e);
                }
            }

            // 循环处理删除退款图片记录，
            for(RefundImg img : refundImgsToDelete) {
                String url = img.getImageUrl();
                try {
                    storageService.delete(url);
                    refundImgRepository.deleteById(img.getId());
                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("删除退款冗余图片失败 URL: {} 错误: {}", url, e.getMessage(), e);
                }
            }

            // 循环处理删除酒店图片记录。需要处理两种情况：有缩略图和无缩略图
            for(AccommodationImg img : accommodationImgsToDelete) {
                String url = img.getUrl();
                String thumbnailUrl = img.getThumbnailUrl();
                try {
                    // 先删云端缩略图（如果有）
                    if(thumbnailUrl != null) {
                        try {
                            storageService.delete(thumbnailUrl);
                        } catch (Exception e) {
                            logger.warn("删除酒店冗余图片缩略图失败 URL: {} 错误: {}", thumbnailUrl, e.getMessage(), e);
                        }
                    }
                    // 再删云端原图
                    storageService.delete(url);
                    // 最后删数据库记录
                    accommodationImgRepository.deleteById(img.getId());
                    deletedCount++;
                } catch (Exception e) {
                    logger.warn("删除酒店冗余图片失败 URL: {} 错误: {}", url, e.getMessage(), e);
                }
            }

            logger.info("已尝试清理 {} 张业务冗余图片", PdImgsToDelete.size() + avatarImgsToDelete.size() + AIImgsToDelete.size() + shopBannerImgsToDelete.size() + refundImgsToDelete.size() + accommodationImgsToDelete.size());
                logger.info("成功清理 {} 张业务冗余图片", deletedCount);
        } catch (Exception e) {
            logger.error("自动清理业务冗余图片失败: {}", e.getMessage(), e);
        }
    }
}