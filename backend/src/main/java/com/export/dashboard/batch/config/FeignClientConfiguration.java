package com.export.dashboard.batch.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign Client 설정
 * 관세청 API 호출을 위한 Feign 클라이언트 구성
 */
@Configuration
@EnableFeignClients(basePackages = "com.export.dashboard.batch.service")
public class FeignClientConfiguration {

    @Value("${customs.api.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${customs.api.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            connectTimeout, TimeUnit.MILLISECONDS,
            readTimeout, TimeUnit.MILLISECONDS,
            true
        );
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Retryer retryer() {
        // Resilience4j의 retry를 사용하므로 Feign의 기본 재시도는 비활성화
        return Retryer.NEVER_RETRY;
    }
}
