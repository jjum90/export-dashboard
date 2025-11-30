package com.export.dashboard.batch.config;

import org.quartz.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz Scheduler 설정
 * 관세청 데이터 동기화 배치 작업 스케줄링
 */
@Configuration
public class QuartzSchedulerConfiguration {

    private final CustomsBatchProperties batchProperties;

    public QuartzSchedulerConfiguration(CustomsBatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }

    /**
     * JobRegistryBeanPostProcessor
     * Job을 JobRegistry에 자동 등록
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        return postProcessor;
    }

    /**
     * Quartz JobDetail
     * 실행할 Job 정의
     */
    @Bean
    public JobDetail customsDataSyncJobDetail() {
        return JobBuilder.newJob(CustomsDataSyncQuartzJob.class)
            .withIdentity("customsDataSyncJob")
            .storeDurably()
            .build();
    }

    /**
     * Quartz Trigger
     * Job 실행 스케줄 정의 (Cron 표현식)
     */
    @Bean
    public Trigger customsDataSyncTrigger(@Qualifier("customsDataSyncJobDetail") JobDetail jobDetail) {
        if (!batchProperties.isEnabled()) {
            // 배치 비활성화 시 스케줄 등록 안함
            return null;
        }

        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("customsDataSyncTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule(batchProperties.getCron()))
            .build();
    }

    /**
     * Quartz Job 클래스
     * Spring Batch Job을 Quartz Job으로 래핑
     */
    public static class CustomsDataSyncQuartzJob implements org.quartz.Job {

        private final JobLauncher jobLauncher;
        private final JobRegistry jobRegistry;

        public CustomsDataSyncQuartzJob(JobLauncher jobLauncher, JobRegistry jobRegistry) {
            this.jobLauncher = jobLauncher;
            this.jobRegistry = jobRegistry;
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            try {
                Job job = jobRegistry.getJob("customsDataSyncJob");

                org.springframework.batch.core.JobParameters jobParameters =
                    new org.springframework.batch.core.JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters();

                jobLauncher.run(job, jobParameters);

            } catch (Exception e) {
                throw new JobExecutionException("관세청 데이터 동기화 배치 실행 실패", e);
            }
        }
    }
}
