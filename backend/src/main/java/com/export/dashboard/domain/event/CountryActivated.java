package com.export.dashboard.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 국가 활성화 이벤트
 */
public record CountryActivated(
    UUID eventId,
    Instant occurredOn,
    String aggregateId,
    CountryData data
) implements DomainEvent {

    @Override
    public String eventType() {
        return "CountryActivated";
    }
}