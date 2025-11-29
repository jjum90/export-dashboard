package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CountryCode Value Object 테스트")
class CountryCodeTest {

    @Test
    @DisplayName("올바른 국가 코드로 CountryCode 생성")
    void createValidCountryCode() {
        // given
        String validCode = "KOR";

        // when
        CountryCode countryCode = CountryCode.from(validCode);

        // then
        assertThat(countryCode.value()).isEqualTo("KOR");
    }

    @Test
    @DisplayName("소문자 국가 코드는 대문자로 변환됨")
    void convertLowercaseToUppercase() {
        // given
        String lowercaseCode = "kor";

        // when
        CountryCode countryCode = CountryCode.from(lowercaseCode);

        // then
        assertThat(countryCode.value()).isEqualTo("KOR");
    }

    @Test
    @DisplayName("공백이 포함된 국가 코드는 트림됨")
    void trimWhitespace() {
        // given
        String codeWithWhitespace = " KOR ";

        // when
        CountryCode countryCode = CountryCode.from(codeWithWhitespace);

        // then
        assertThat(countryCode.value()).isEqualTo("KOR");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("null, 빈 문자열, 공백만 있는 경우 예외 발생")
    void throwExceptionForNullOrEmpty(String invalidCode) {
        // when & then
        assertThatThrownBy(() -> CountryCode.from(invalidCode))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("국가 코드는 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"K", "KO", "KOREA", "KR1", "K@R", "한국"})
    @DisplayName("3자리 대문자 알파벳이 아닌 경우 예외 발생")
    void throwExceptionForInvalidFormat(String invalidCode) {
        // when & then
        assertThatThrownBy(() -> CountryCode.from(invalidCode))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessageContaining("3자리 대문자 알파벳이어야 합니다");
    }

    @Test
    @DisplayName("동일한 값의 CountryCode는 같음")
    void equalityTest() {
        // given
        CountryCode code1 = CountryCode.from("KOR");
        CountryCode code2 = CountryCode.from("kor");
        CountryCode code3 = CountryCode.from("USA");

        // when & then
        assertThat(code1).isEqualTo(code2);
        assertThat(code1).isNotEqualTo(code3);
        assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
    }

    @Test
    @DisplayName("CountryCode.of() 팩토리 메서드 테스트")
    void factoryMethodOf() {
        // given
        String code = "USA";

        // when
        CountryCode countryCode = CountryCode.of(code);

        // then
        assertThat(countryCode.value()).isEqualTo("USA");
    }

    @Test
    @DisplayName("toString() 메서드는 값 자체를 반환")
    void toStringTest() {
        // given
        CountryCode countryCode = CountryCode.from("JPN");

        // when
        String result = countryCode.toString();

        // then
        assertThat(result).isEqualTo("JPN");
    }
}