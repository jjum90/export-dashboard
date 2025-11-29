package com.export.dashboard.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 수출 임계값 초과 이벤트
 * 비즈니스 룰에 따른 경보성 이벤트
 */
public record ExportThresholdExceeded(
    UUID eventId,
    Instant occurredOn,
    String aggregateId,
    ThresholdData data
) implements DomainEvent {

    @Override
    public String eventType() {
        return "ExportThresholdExceeded";
    }
}