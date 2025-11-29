package com.export.dashboard.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 상품 카테고리 생성 이벤트
 */
public record ProductCategoryCreated(
    UUID eventId,
    Instant occurredOn,
    String aggregateId,
    ProductCategoryData data
) implements DomainEvent {

    @Override
    public String eventType() {
        return "ProductCategoryCreated";
    }
}