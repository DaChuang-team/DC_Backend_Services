package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.AIInteractionImg;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.UserAvatarRecord;
import org.dachuang_team.dc_backend_services.domain.PO.ProductPO.ProductImageRecord;
import org.dachuang_team.dc_backend_services.repository.AIInteractionImgRepository;
import org.dachuang_team.dc_backend_services.repository.ProductImageRecordRepository;
import org.dachuang_team.dc_backend_services.repository.UserAvatarRecordRepository;
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
    private ProductImageRecordRepository productImageRecordRepository;

    @Autowired
    private UserAvatarRecordRepository userAvatarRecordRepository;

    @Autowired
    private AIInteractionImgRepository aiInteractionImgRepository;

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
            List<ProductImageRecord> PdRecordsToDelete = productImageRecordRepository
                    .findAllByCreatedAtBeforeAndIsLinkedFalse(threeDaysAgo);

            List<UserAvatarRecord> avatarRecordsToDelete = userAvatarRecordRepository
                    .findAllByUploadAtBeforeAndIsLinkedFalse(threeDaysAgo);

            List<AIInteractionImg> AIRecordToDelete =
                    aiInteractionImgRepository.findAllByUploadTimeBefore(threeDaysAgo);

            if (PdRecordsToDelete.isEmpty() && avatarRecordsToDelete.isEmpty() && AIRecordToDelete.isEmpty()) {
                logger.info("没有需要清理的图片资源");
                return;
            }

            // 循环处理删除商品图片记录
            for (ProductImageRecord record : PdRecordsToDelete) {
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
            for (UserAvatarRecord record : avatarRecordsToDelete) {
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
            for (AIInteractionImg img : AIRecordToDelete) {
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

            logger.info("已尝试清理 {} 张业务冗余图片", PdRecordsToDelete.size() + avatarRecordsToDelete.size() + AIRecordToDelete.size());
                logger.info("成功清理 {} 张业务冗余图片", deletedCount);
        } catch (Exception e) {
            logger.error("自动清理业务冗余图片失败: {}", e.getMessage(), e);
        }
    }
}