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

    /**
     * 이벤트 생성을 위한 팩토리 메서드
     */
    @SuppressWarnings("unchecked")
    static <T extends DomainEvent> T create(Class<T> eventType, String aggregateId, Object eventData) {
        var eventId = UUID.randomUUID();
        var occurredOn = Instant.now();

        String typeName = eventType.getSimpleName();

        if ("ExportStatisticCreated".equals(typeName)) {
            return eventType.cast(
                new ExportStatisticCreated(eventId, occurredOn, aggregateId, (ExportStatisticData) eventData)
            );
        } else if ("ExportStatisticUpdated".equals(typeName)) {
            return eventType.cast(
                new ExportStatisticUpdated(eventId, occurredOn, aggregateId, (ExportStatisticData) eventData)
            );
        } else if ("CountryActivated".equals(typeName)) {
            return eventType.cast(
                new CountryActivated(eventId, occurredOn, aggregateId, (CountryData) eventData)
            );
        } else if ("CountryDeactivated".equals(typeName)) {
            return eventType.cast(
                new CountryDeactivated(eventId, occurredOn, aggregateId, (CountryData) eventData)
            );
        } else if ("ProductCategoryCreated".equals(typeName)) {
            return eventType.cast(
                new ProductCategoryCreated(eventId, occurredOn, aggregateId, (ProductCategoryData) eventData)
            );
        } else if ("ExportThresholdExceeded".equals(typeName)) {
            return eventType.cast(
                new ExportThresholdExceeded(eventId, occurredOn, aggregateId, (ThresholdData) eventData)
            );
        } else {
            throw new IllegalArgumentException("지원하지 않는 이벤트 타입: " + typeName);
        }
    }
}