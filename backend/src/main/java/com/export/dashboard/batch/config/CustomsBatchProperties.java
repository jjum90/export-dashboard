package com.export.dashboard.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 관세청 배치 작업 설정 프로퍼티
 */
@Configuration
@ConfigurationProperties(prefix = "batch.customs-sync")
public class CustomsBatchProperties {

    private boolean enabled = true;
    private String cron = "0 0 2 1 * ?"; // 매월 1일 새벽 2시
    private int chunkSize = 10;
    private String excelFilePath = "classpath:data/it-product-codes.xlsx";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getExcelFilePath() {
        return excelFilePath;
    }

    public void setExcelFilePath(String excelFilePath) {
        this.excelFilePath = excelFilePath;
    }
}
