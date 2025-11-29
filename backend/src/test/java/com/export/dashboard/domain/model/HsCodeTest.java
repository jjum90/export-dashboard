package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HsCode Value Object 테스트")
class HsCodeTest {

    @ParameterizedTest
    @CsvSource({
        "01, 1",
        "0101, 2",
        "010101, 3",
        "01010101, 4",
        "0101010101, 5"
    })
    @DisplayName("올바른 HS 코드와 레벨로 HsCode 생성")
    void createValidHsCode(String code, Integer level) {
        // when
        HsCode hsCode = HsCode.from(code, level);

        // then
        assertThat(hsCode.value()).isEqualTo(code);
        assertThat(hsCode.level()).isEqualTo(level);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("null, 빈 문자열, 공백만 있는 경우 예외 발생")
    void throwExceptionForNullOrEmpty(String invalidCode) {
        // when & then
        assertThatThrownBy(() -> HsCode.from(invalidCode, 1))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("HS 코드는 필수입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1234567890123", "12A3", "AB12", "12.34"})
    @DisplayName("잘못된 형식의 HS 코드는 예외 발생")
    void throwExceptionForInvalidFormat(String invalidCode) {
        // when & then
        assertThatThrownBy(() -> HsCode.from(invalidCode, 2))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessageContaining("2-10자리 숫자여야 합니다");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, 10, -1})
    @DisplayName("잘못된 레벨은 예외 발생")
    void throwExceptionForInvalidLevel(Integer invalidLevel) {
        // when & then
        assertThatThrownBy(() -> HsCode.from("1234", invalidLevel))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessageContaining("HS 코드 레벨은 1-6 사이여야 합니다");
    }

    @ParameterizedTest
    @CsvSource({
        "123, 2",  // 레벨 2는 4자리여야 함
        "12345, 2", // 레벨 2는 4자리여야 함
        "1234, 1",  // 레벨 1은 2자리여야 함
        "123456, 2" // 레벨 2는 4자리여야 함
    })
    @DisplayName("레벨과 자릿수가 일치하지 않으면 예외 발생")
    void throwExceptionForMismatchedLevelAndLength(String code, Integer level) {
        // when & then
        assertThatThrownBy(() -> HsCode.from(code, level))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessageContaining("자리여야 합니다");
    }

    @Test
    @DisplayName("공백이 포함된 HS 코드는 트림됨")
    void trimWhitespace() {
        // given
        String codeWithWhitespace = " 1234 ";

        // when
        HsCode hsCode = HsCode.from(codeWithWhitespace, 2);

        // then
        assertThat(hsCode.value()).isEqualTo("1234");
    }

    @Test
    @DisplayName("상위 레벨 HS 코드 반환")
    void getParent() {
        // given
        HsCode level3Code = HsCode.from("123456", 3);

        // when
        HsCode parentCode = level3Code.getParent();

        // then
        assertThat(parentCode.value()).isEqualTo("1234");
        assertThat(parentCode.level()).isEqualTo(2);
    }

    @Test
    @DisplayName("레벨 1 HS 코드는 상위 코드가 없음")
    void throwExceptionWhenGettingParentOfLevel1() {
        // given
        HsCode level1Code = HsCode.from("12", 1);

        // when & then
        assertThatThrownBy(level1Code::getParent)
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("레벨 1 HS 코드는 상위 코드가 없습니다.");
    }

    @Test
    @DisplayName("챕터(최상위 레벨) HS 코드 반환")
    void getChapter() {
        // given
        HsCode level4Code = HsCode.from("12345678", 4);

        // when
        HsCode chapterCode = level4Code.getChapter();

        // then
        assertThat(chapterCode.value()).isEqualTo("12");
        assertThat(chapterCode.level()).isEqualTo(1);
    }

    @Test
    @DisplayName("레벨 1 코드의 챕터는 자기 자신")
    void getChapterOfLevel1Code() {
        // given
        HsCode level1Code = HsCode.from("12", 1);

        // when
        HsCode chapterCode = level1Code.getChapter();

        // then
        assertThat(chapterCode).isEqualTo(level1Code);
    }

    @Test
    @DisplayName("동일한 값과 레벨의 HsCode는 같음")
    void equalityTest() {
        // given
        HsCode code1 = HsCode.from("1234", 2);
        HsCode code2 = HsCode.from("1234", 2);
        HsCode code3 = HsCode.from("5678", 2);
        HsCode code4 = HsCode.from("12", 1);

        // when & then
        assertThat(code1).isEqualTo(code2);
        assertThat(code1).isNotEqualTo(code3);
        assertThat(code1).isNotEqualTo(code4);
        assertThat(code1.hashCode()).isEqualTo(code2.hashCode());
    }

    @Test
    @DisplayName("HsCode.of() 팩토리 메서드 테스트")
    void factoryMethodOf() {
        // given
        String code = "123456";
        Integer level = 3;

        // when
        HsCode hsCode = HsCode.of(code, level);

        // then
        assertThat(hsCode.value()).isEqualTo("123456");
        assertThat(hsCode.level()).isEqualTo(3);
    }

    @Test
    @DisplayName("toString() 메서드는 값 자체를 반환")
    void toStringTest() {
        // given
        HsCode hsCode = HsCode.from("123456", 3);

        // when
        String result = hsCode.toString();

        // then
        assertThat(result).isEqualTo("123456");
    }
}