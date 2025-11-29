package com.export.dashboard.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * 수출 통계 업데이트 이벤트
 */
public record ExportStatisticUpdated(
    UUID eventId,
    Instant occurredOn,
    String aggregateId,
    ExportStatisticData data
) implements DomainEvent {

    public ExportStatisticUpdated {
        if (eventId == null) {
            throw new IllegalArgumentException("이벤트 ID는 필수입니다");
        }
        if (occurredOn == null) {
            throw new IllegalArgumentException("발생 시간은 필수입니다");
        }
        if (aggregateId == null || aggregateId.trim().isEmpty()) {
            throw new IllegalArgumentException("집계 ID는 필수입니다");
        }
        if (data == null) {
            throw new IllegalArgumentException("이벤트 데이터는 필수입니다");
        }
    }

    @Override
    public String eventType() {
        return "ExportStatisticUpdated";
    }
}