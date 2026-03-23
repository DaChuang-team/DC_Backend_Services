package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.pojo.ProductPO.ProductImageRecord;
import org.dachuang_team.dc_backend_services.repository.ProductImageRecordRepository;
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
public class ProductImgCleanupTask {

    @Autowired
    private ProductImageRecordRepository productImageRecordRepository;

    @Autowired
    private IStorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(ProductImgCleanupTask.class);

    // 程序完全启动就绪后执行一次
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        cleanOldUnlinkedImages();
    }

    // 每天凌晨一点执行一次
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional // 确保删除操作在事务中执行
    public void cleanOldUnlinkedImages() {
        logger.info("开始执行商品冗余图片清理任务");
        int deletedCount = 0;
        try {
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

            // 查询符合条件的记录
            List<ProductImageRecord> recordsToDelete = productImageRecordRepository
                    .findAllByCreatedAtBeforeAndIsLinkedFalse(threeDaysAgo);

            if (recordsToDelete.isEmpty()) {
                logger.info("没有需要清理的图片资源");
                return;
            }

            // 循环处理删除
            for (ProductImageRecord record : recordsToDelete) {
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
            logger.info("已尝试清理 {} 张冗余商品图片", recordsToDelete.size());
                logger.info("成功清理 {} 张冗余商品图片", deletedCount);
        } catch (Exception e) {
            logger.error("自动清理商品冗余图片失败: {}", e.getMessage(), e);
        }
    }
}