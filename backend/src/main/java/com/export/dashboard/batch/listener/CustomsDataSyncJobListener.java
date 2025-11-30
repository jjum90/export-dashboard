package com.export.dashboard.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 관세청 데이터 동기화 Job Listener
 * Job 시작/종료 시 로깅 및 모니터링
 */
@Component
public class CustomsDataSyncJobListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomsDataSyncJobListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("========================================");
        logger.info("관세청 데이터 동기화 배치 시작");
        logger.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
        logger.info("Job ID: {}", jobExecution.getJobId());
        logger.info("Start Time: {}", LocalDateTime.now());
        logger.info("========================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(
            jobExecution.getStartTime().toInstant(),
            jobExecution.getEndTime().toInstant()
        );

        logger.info("========================================");
        logger.info("관세청 데이터 동기화 배치 종료");
        logger.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
        logger.info("Job ID: {}", jobExecution.getJobId());
        logger.info("Status: {}", jobExecution.getStatus());
        logger.info("Exit Status: {}", jobExecution.getExitStatus().getExitCode());
        logger.info("Duration: {} seconds", duration.getSeconds());

        if (jobExecution.getAllFailureExceptions().isEmpty()) {
            logger.info("배치 작업 성공");
        } else {
            logger.error("배치 작업 실패");
            jobExecution.getAllFailureExceptions().forEach(throwable ->
                logger.error("Failure: {}", throwable.getMessage(), throwable)
            );
        }

        logger.info("========================================");
    }
}
