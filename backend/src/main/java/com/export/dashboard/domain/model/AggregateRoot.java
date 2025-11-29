package com.export.dashboard.domain.model;

import com.export.dashboard.domain.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root의 기본 클래스
 * 도메인 이벤트 발행 기능을 포함한 DDD 패턴 구현
 */
public abstract class AggregateRoot {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 도메인 이벤트 등록
     */
    protected void registerEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    /**
     * 등록된 도메인 이벤트 목록 조회 (읽기 전용)
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 도메인 이벤트 목록 초기화
     * 이벤트 발행 후 호출되어야 함
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * 등록된 이벤트가 있는지 확인
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * 특정 타입의 이벤트가 등록되어 있는지 확인
     */
    public boolean hasEventOfType(Class<? extends DomainEvent> eventType) {
        return domainEvents.stream()
                          .anyMatch(event -> eventType.isAssignableFrom(event.getClass()));
    }

    /**
     * 집계 루트의 고유 식별자 반환
     * 하위 클래스에서 구현 필요
     */
    public abstract String getAggregateId();
}