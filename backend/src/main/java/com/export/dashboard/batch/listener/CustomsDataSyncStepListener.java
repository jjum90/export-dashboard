package com.export.dashboard.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 관세청 데이터 동기화 Step Listener
 * Step 시작/종료 시 로깅 및 통계 정보 수집
 */
@Component
public class CustomsDataSyncStepListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomsDataSyncStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("--- Step 시작: {} ---", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info("--- Step 종료: {} ---", stepExecution.getStepName());
        logger.info("Read Count: {}", stepExecution.getReadCount());
        logger.info("Write Count: {}", stepExecution.getWriteCount());
        logger.info("Commit Count: {}", stepExecution.getCommitCount());
        logger.info("Rollback Count: {}", stepExecution.getRollbackCount());
        logger.info("Skip Count: {}", stepExecution.getSkipCount());
        logger.info("Filter Count: {}", stepExecution.getFilterCount());

        if (stepExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
            logger.error("Step 실패: {}", stepExecution.getStepName());
            stepExecution.getFailureExceptions().forEach(throwable ->
                logger.error("Failure Exception: {}", throwable.getMessage(), throwable)
            );
        }

        return stepExecution.getExitStatus();
    }
}
