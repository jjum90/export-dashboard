package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;

import java.time.YearMonth;

/**
 * 수출 통계의 기간(년월)을 나타내는 Value Object
 */
public record ExportPeriod(Integer year, Integer month) {

    public ExportPeriod {
        if (year == null) {
            throw new InvalidValueObjectException("년도는 필수입니다.");
        }

        if (month == null) {
            throw new InvalidValueObjectException("월은 필수입니다.");
        }

        if (year < 1900 || year > 2100) {
            throw new InvalidValueObjectException(
                "년도는 1900-2100 사이여야 합니다. 입력값: " + year
            );
        }

        if (month < 1 || month > 12) {
            throw new InvalidValueObjectException(
                "월은 1-12 사이여야 합니다. 입력값: " + month
            );
        }
    }

    public static ExportPeriod of(Integer year, Integer month) {
        return new ExportPeriod(year, month);
    }

    public static ExportPeriod from(YearMonth yearMonth) {
        return new ExportPeriod(yearMonth.getYear(), yearMonth.getMonthValue());
    }

    public static ExportPeriod current() {
        YearMonth now = YearMonth.now();
        return new ExportPeriod(now.getYear(), now.getMonthValue());
    }

    public YearMonth toYearMonth() {
        return YearMonth.of(year, month);
    }

    /**
     * 이전 월 반환
     */
    public ExportPeriod previousMonth() {
        YearMonth current = YearMonth.of(year, month);
        YearMonth previous = current.minusMonths(1);
        return new ExportPeriod(previous.getYear(), previous.getMonthValue());
    }

    /**
     * 다음 월 반환
     */
    public ExportPeriod nextMonth() {
        YearMonth current = YearMonth.of(year, month);
        YearMonth next = current.plusMonths(1);
        return new ExportPeriod(next.getYear(), next.getMonthValue());
    }

    /**
     * 같은 년도의 이전 년도 같은 월 반환 (전년 동월)
     */
    public ExportPeriod sameMonthPreviousYear() {
        return new ExportPeriod(year - 1, month);
    }

    /**
     * 해당 년도의 첫 월
     */
    public ExportPeriod firstMonthOfYear() {
        return new ExportPeriod(year, 1);
    }

    /**
     * 해당 년도의 마지막 월
     */
    public ExportPeriod lastMonthOfYear() {
        return new ExportPeriod(year, 12);
    }

    /**
     * 다른 기간과의 비교 (이후인지)
     */
    public boolean isAfter(ExportPeriod other) {
        return this.toYearMonth().isAfter(other.toYearMonth());
    }

    /**
     * 다른 기간과의 비교 (이전인지)
     */
    public boolean isBefore(ExportPeriod other) {
        return this.toYearMonth().isBefore(other.toYearMonth());
    }

    @Override
    public String toString() {
        return String.format("%04d-%02d", year, month);
    }
}