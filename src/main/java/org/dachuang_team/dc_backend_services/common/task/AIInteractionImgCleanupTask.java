package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.pojo.ImgPO.AIInteractionImg;
import org.dachuang_team.dc_backend_services.repository.AIInteractionImgRepository;
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
public class AIInteractionImgCleanupTask {

    @Autowired
    private AIInteractionImgRepository aiInteractionImgRepository;

    @Autowired
    private IStorageService storageService;

    private static final Logger logger = LoggerFactory.getLogger(AIInteractionImgCleanupTask.class);

    // 程序完全启动就绪后执行一次
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        cleanOldImages();
    }

    // 每天凌晨一点执行一次
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void cleanOldImages() {
        logger.info("开始执行AI交互缓存图片清理任务");
        int deletedCount = 0;
        try {
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
            List<AIInteractionImg> recordToDelete =
                    aiInteractionImgRepository.findAllByUploadTimeBefore(threeDaysAgo);

            if(recordToDelete.isEmpty()) {
                logger.info("没有需要清理的图片资源");
                return;
            }

            for(AIInteractionImg img : recordToDelete) {
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
                logger.info("已尝试删除 {} 张AI交互缓存图片", recordToDelete.size());
                logger.info("成功删除 {} 张AI交互缓存图片", deletedCount);
            }

        } catch (Exception e) {
            logger.error("自动清理AI交互缓存图片失败: {}", e.getMessage(), e);
        }
    }
}