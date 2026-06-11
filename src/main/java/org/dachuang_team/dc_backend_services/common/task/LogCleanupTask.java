package org.dachuang_team.dc_backend_services.common.task;

import org.dachuang_team.dc_backend_services.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class LogCleanupTask {

    @Autowired
    private OperationLogRepository operationLogRepository;

    private static final Logger logger = LoggerFactory.getLogger(LogCleanupTask.class);

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(15);
        logger.info("开始清理 {} 之前的操作日志", cutoff);
        try {
            long deleted = operationLogRepository.deleteByCreatedAtBefore(cutoff);
            logger.info("操作日志清理完成，共删除 {} 条记录", deleted);
        } catch (Exception e) {
            logger.error("操作日志清理失败: {}", e.getMessage(), e);
        }
    }
}
