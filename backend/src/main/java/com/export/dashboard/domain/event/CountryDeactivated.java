package com.export.dashboard.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 국가 비활성화 이벤트
 */
public record CountryDeactivated(
    UUID eventId,
    Instant occurredOn,
    String aggregateId,
    CountryData data
) implements DomainEvent {

    @Override
    public String eventType() {
        return "CountryDeactivated";
    }
}