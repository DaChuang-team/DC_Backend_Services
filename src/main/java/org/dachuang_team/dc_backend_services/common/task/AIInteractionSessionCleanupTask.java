package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.repository.AISessionContextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@EnableScheduling
public class AIInteractionSessionCleanupTask {

    @Autowired
    private AISessionContextRepository aiSessionContextRepository;

    private static final Logger logger = LoggerFactory.getLogger(AIInteractionSessionCleanupTask.class);

    // 程序启动时执行一次
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        cleanupExpiredSessions();
    }

    // 每天凌晨四点执行一次
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExpiredSessions() {
        logger.info("开始执行AI交互会话上下文清理任务");
        int recordsToDelete = aiSessionContextRepository.countByExpireTimeBefore(LocalDateTime.now());
        logger.info("已找到" + recordsToDelete + "条过期的上下文记录");
        if(recordsToDelete > 0) {
            aiSessionContextRepository.deleteByExpireTimeBefore(LocalDateTime.now());
            logger.info("成功清理了" + recordsToDelete + "条过期的上下文记录");
        } else {
            logger.info("没有过期的上下文记录需要清理");
        }
    }
}
