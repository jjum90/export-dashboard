package com.export.dashboard.batch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 기본 설정
 * 배치 작업 관련 기본 빈 및 인프라 설정
 */
@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    // Spring Batch 자동 구성 활성화
}
