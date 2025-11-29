package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object 테스트")
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency KRW = Currency.getInstance("KRW");

    @Test
    @DisplayName("올바른 금액과 통화로 Money 생성")
    void createValidMoney() {
        // given
        BigDecimal amount = BigDecimal.valueOf(100.50);

        // when
        Money money = Money.of(amount, USD);

        // then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
        assertThat(money.currency()).isEqualTo(USD);
    }

    @Test
    @DisplayName("null 금액으로 Money 생성 시 예외 발생")
    void throwExceptionForNullAmount() {
        // when & then
        assertThatThrownBy(() -> Money.of(null, USD))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("금액은 필수입니다.");
    }

    @Test
    @DisplayName("null 통화로 Money 생성 시 예외 발생")
    void throwExceptionForNullCurrency() {
        // when & then
        assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(100), null))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("통화는 필수입니다.");
    }

    @Test
    @DisplayName("음수 금액으로 Money 생성 시 예외 발생")
    void throwExceptionForNegativeAmount() {
        // given
        BigDecimal negativeAmount = BigDecimal.valueOf(-100);

        // when & then
        assertThatThrownBy(() -> Money.of(negativeAmount, USD))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("금액은 0 이상이어야 합니다. 입력값: -100");
    }

    @Test
    @DisplayName("0 금액은 허용됨")
    void allowZeroAmount() {
        // given
        BigDecimal zeroAmount = BigDecimal.ZERO;

        // when
        Money money = Money.of(zeroAmount, USD);

        // then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money.isZero()).isTrue();
        assertThat(money.isPositive()).isFalse();
    }

    @Test
    @DisplayName("USD 금액 생성 팩토리 메서드")
    void createUsdMoney() {
        // given
        BigDecimal amount = BigDecimal.valueOf(1000);

        // when
        Money money = Money.usd(amount);

        // then
        assertThat(money.currency()).isEqualTo(USD);
        assertThat(money.amount()).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("KRW 금액 생성 팩토리 메서드")
    void createKrwMoney() {
        // given
        BigDecimal amount = BigDecimal.valueOf(1000000);

        // when
        Money money = Money.krw(amount);

        // then
        assertThat(money.currency()).isEqualTo(KRW);
        assertThat(money.amount()).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("0 금액 생성 팩토리 메서드")
    void createZeroMoney() {
        // when
        Money money = Money.zero(USD);

        // then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money.currency()).isEqualTo(USD);
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("같은 통화끼리 더하기")
    void addSameCurrency() {
        // given
        Money money1 = Money.usd(BigDecimal.valueOf(100));
        Money money2 = Money.usd(BigDecimal.valueOf(50));

        // when
        Money result = money1.add(money2);

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(result.currency()).isEqualTo(USD);
    }

    @Test
    @DisplayName("다른 통화끼리 더하기 시 예외 발생")
    void throwExceptionWhenAddingDifferentCurrencies() {
        // given
        Money usdMoney = Money.usd(BigDecimal.valueOf(100));
        Money krwMoney = Money.krw(BigDecimal.valueOf(100000));

        // when & then
        assertThatThrownBy(() -> usdMoney.add(krwMoney))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("통화가 일치하지 않습니다. USD != KRW");
    }

    @Test
    @DisplayName("같은 통화끼리 빼기")
    void subtractSameCurrency() {
        // given
        Money money1 = Money.usd(BigDecimal.valueOf(100));
        Money money2 = Money.usd(BigDecimal.valueOf(30));

        // when
        Money result = money1.subtract(money2);

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(70));
        assertThat(result.currency()).isEqualTo(USD);
    }

    @Test
    @DisplayName("뺄셈 결과가 음수가 되면 예외 발생")
    void throwExceptionWhenSubtractionResultsInNegative() {
        // given
        Money money1 = Money.usd(BigDecimal.valueOf(50));
        Money money2 = Money.usd(BigDecimal.valueOf(100));

        // when & then
        assertThatThrownBy(() -> money1.subtract(money2))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("결과가 음수가 될 수 없습니다.");
    }

    @Test
    @DisplayName("곱하기")
    void multiply() {
        // given
        Money money = Money.usd(BigDecimal.valueOf(100));
        BigDecimal multiplier = BigDecimal.valueOf(2.5);

        // when
        Money result = money.multiply(multiplier);

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
        assertThat(result.currency()).isEqualTo(USD);
    }

    @Test
    @DisplayName("음수로 곱하기 시 예외 발생")
    void throwExceptionWhenMultiplyingByNegative() {
        // given
        Money money = Money.usd(BigDecimal.valueOf(100));
        BigDecimal negativeMultiplier = BigDecimal.valueOf(-2);

        // when & then
        assertThatThrownBy(() -> money.multiply(negativeMultiplier))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("승수는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("나누기")
    void divide() {
        // given
        Money money = Money.usd(BigDecimal.valueOf(100));
        BigDecimal divisor = BigDecimal.valueOf(4);

        // when
        Money result = money.divide(divisor);

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
        assertThat(result.currency()).isEqualTo(USD);
    }

    @Test
    @DisplayName("0으로 나누기 시 예외 발생")
    void throwExceptionWhenDividingByZero() {
        // given
        Money money = Money.usd(BigDecimal.valueOf(100));

        // when & then
        assertThatThrownBy(() -> money.divide(BigDecimal.ZERO))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("제수는 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("백분율 계산")
    void calculatePercentage() {
        // given
        Money part = Money.usd(BigDecimal.valueOf(25));
        Money total = Money.usd(BigDecimal.valueOf(100));

        // when
        BigDecimal percentage = part.percentageOf(total);

        // then
        assertThat(percentage).isEqualByComparingTo(BigDecimal.valueOf(25.0000));
    }

    @Test
    @DisplayName("전체가 0일 때 백분율은 0")
    void percentageOfZeroTotal() {
        // given
        Money part = Money.usd(BigDecimal.valueOf(25));
        Money zeroTotal = Money.zero(USD);

        // when
        BigDecimal percentage = part.percentageOf(zeroTotal);

        // then
        assertThat(percentage).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00"})
    @DisplayName("0인지 확인")
    void isZero(String amountStr) {
        // given
        Money money = Money.usd(new BigDecimal(amountStr));

        // when & then
        assertThat(money.isZero()).isTrue();
        assertThat(money.isPositive()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "1", "100.50"})
    @DisplayName("양수인지 확인")
    void isPositive(String amountStr) {
        // given
        Money money = Money.usd(new BigDecimal(amountStr));

        // when & then
        assertThat(money.isPositive()).isTrue();
        assertThat(money.isZero()).isFalse();
    }

    @Test
    @DisplayName("동일한 금액과 통화의 Money는 같음")
    void equalityTest() {
        // given
        Money money1 = Money.usd(BigDecimal.valueOf(100.50));
        Money money2 = Money.usd(BigDecimal.valueOf(100.50));
        Money money3 = Money.usd(BigDecimal.valueOf(200.00));
        Money money4 = Money.krw(BigDecimal.valueOf(100.50));

        // when & then
        assertThat(money1).isEqualTo(money2);
        assertThat(money1).isNotEqualTo(money3);
        assertThat(money1).isNotEqualTo(money4);
        assertThat(money1.hashCode()).isEqualTo(money2.hashCode());
    }

    @Test
    @DisplayName("toString() 메서드 테스트")
    void toStringTest() {
        // given
        Money money = Money.usd(BigDecimal.valueOf(1234.56));

        // when
        String result = money.toString();

        // then
        assertThat(result).isEqualTo("1234.56 USD");
    }

    @Test
    @DisplayName("스케일 정규화 테스트")
    void scaleNormalization() {
        // given
        BigDecimal amountWithManyDecimals = new BigDecimal("100.123456789");

        // when
        Money money = Money.usd(amountWithManyDecimals);

        // then
        assertThat(money.amount().scale()).isEqualTo(2);
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.12"));
    }
}