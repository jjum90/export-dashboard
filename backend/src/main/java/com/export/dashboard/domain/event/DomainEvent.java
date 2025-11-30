package com.export.dashboard.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 도메인 이벤트의 기본 인터페이스
 * Java 17 호환을 위해 일반 인터페이스 사용
 */
public interface DomainEvent {

    /**
     * 이벤트 고유 식별자
     */
    UUID eventId();

    /**
     * 이벤트 발생 시각
     */
    Instant occurredOn();

    /**
     * 이벤트 타입
     */
    String eventType();

    /**
     * 집계 루트 식별자
     */
    String aggregateId();

    /**
     * 이벤트 버전 (이벤트 스키마 버전)
     */
    default int version() {
        return 1;
    }
}
