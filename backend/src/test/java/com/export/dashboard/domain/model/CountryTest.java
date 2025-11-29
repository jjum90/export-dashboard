package com.export.dashboard.domain.model;

import com.export.dashboard.domain.exception.InvalidValueObjectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Country Aggregate 테스트")
class CountryTest {

    @Test
    @DisplayName("올바른 정보로 Country 생성")
    void createValidCountry() {
        // given
        CountryCode countryCode = CountryCode.from("KOR");
        String nameKo = "대한민국";
        String nameEn = "South Korea";

        // when
        Country country = new Country(countryCode, nameKo, nameEn);

        // then
        assertThat(country.getCountryCode()).isEqualTo(countryCode);
        assertThat(country.getNameKo()).isEqualTo(nameKo);
        assertThat(country.getNameEn()).isEqualTo(nameEn);
        assertThat(country.isActive()).isTrue();
    }

    @Test
    @DisplayName("팩토리 메서드로 Country 생성")
    void createCountryWithFactoryMethod() {
        // when
        Country country = Country.create("USA", "미국", "United States");

        // then
        assertThat(country.getCountryCode().value()).isEqualTo("USA");
        assertThat(country.getNameKo()).isEqualTo("미국");
        assertThat(country.getNameEn()).isEqualTo("United States");
        assertThat(country.isActive()).isTrue();
    }

    @Test
    @DisplayName("지역 정보를 포함한 Country 생성")
    void createCountryWithGeographicInfo() {
        // when
        Country country = Country.create("JPN", "일본", "Japan", "동아시아", "아시아");

        // then
        assertThat(country.getRegion()).isEqualTo("동아시아");
        assertThat(country.getContinent()).isEqualTo("아시아");
    }

    @Test
    @DisplayName("null CountryCode로 Country 생성 시 예외 발생")
    void throwExceptionForNullCountryCode() {
        // when & then
        assertThatThrownBy(() -> new Country(null, "대한민국", "South Korea"))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("국가 코드는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("null, 빈 문자열, 공백만 있는 한국어 이름으로 Country 생성 시 예외 발생")
    void throwExceptionForInvalidNameKo(String invalidName) {
        // given
        CountryCode countryCode = CountryCode.from("KOR");

        // when & then
        assertThatThrownBy(() -> new Country(countryCode, invalidName, "South Korea"))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("한국어 국가명은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("null, 빈 문자열, 공백만 있는 영어 이름으로 Country 생성 시 예외 발생")
    void throwExceptionForInvalidNameEn(String invalidName) {
        // given
        CountryCode countryCode = CountryCode.from("KOR");

        // when & then
        assertThatThrownBy(() -> new Country(countryCode, "대한민국", invalidName))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("영어 국가명은 필수입니다.");
    }

    @Test
    @DisplayName("100자 초과 한국어 이름으로 Country 생성 시 예외 발생")
    void throwExceptionForTooLongNameKo() {
        // given
        CountryCode countryCode = CountryCode.from("KOR");
        String tooLongName = "가".repeat(101);

        // when & then
        assertThatThrownBy(() -> new Country(countryCode, tooLongName, "South Korea"))
            .isInstanceOf(InvalidValueObjectException.class)
            .hasMessage("한국어 국가명은 100자를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("이름 공백 트림 테스트")
    void trimNamesWhitespace() {
        // given
        CountryCode countryCode = CountryCode.from("KOR");

        // when
        Country country = new Country(countryCode, " 대한민국 ", " South Korea ");

        // then
        assertThat(country.getNameKo()).isEqualTo("대한민국");
        assertThat(country.getNameEn()).isEqualTo("South Korea");
    }

    @Test
    @DisplayName("국가명 업데이트")
    void updateNames() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea");

        // when
        country.updateNames("한국", "Korea");

        // then
        assertThat(country.getNameKo()).isEqualTo("한국");
        assertThat(country.getNameEn()).isEqualTo("Korea");
    }

    @Test
    @DisplayName("지리적 정보 업데이트")
    void updateGeographicInfo() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea");

        // when
        country.updateGeographicInfo("동북아시아", "아시아");

        // then
        assertThat(country.getRegion()).isEqualTo("동북아시아");
        assertThat(country.getContinent()).isEqualTo("아시아");
    }

    @Test
    @DisplayName("국가 활성화/비활성화")
    void activateDeactivateCountry() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea");

        // when
        country.deactivate();

        // then
        assertThat(country.isActive()).isFalse();

        // when
        country.activate();

        // then
        assertThat(country.isActive()).isTrue();
    }

    @Test
    @DisplayName("지역 소속 확인")
    void belongsToRegion() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea", "동아시아", "아시아");

        // when & then
        assertThat(country.belongsToRegion("동아시아")).isTrue();
        assertThat(country.belongsToRegion("DONGASIA")).isFalse(); // 대소문자 구분 안함
        assertThat(country.belongsToRegion("서유럽")).isFalse();
    }

    @Test
    @DisplayName("대륙 소속 확인")
    void belongsToContinent() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea", "동아시아", "아시아");

        // when & then
        assertThat(country.belongsToContinent("아시아")).isTrue();
        assertThat(country.belongsToContinent("ASIA")).isFalse(); // 대소문자 구분 안함
        assertThat(country.belongsToContinent("유럽")).isFalse();
    }

    @Test
    @DisplayName("지역 정보가 없을 때 소속 확인")
    void belongsToRegionWhenRegionIsNull() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea");

        // when & then
        assertThat(country.belongsToRegion("동아시아")).isFalse();
        assertThat(country.belongsToContinent("아시아")).isFalse();
    }

    @Test
    @DisplayName("동일한 국가 코드의 Country는 같음")
    void equalityTest() {
        // given
        Country country1 = Country.create("KOR", "대한민국", "South Korea");
        Country country2 = Country.create("KOR", "한국", "Korea");
        Country country3 = Country.create("USA", "미국", "United States");

        // when & then
        assertThat(country1).isEqualTo(country2);
        assertThat(country1).isNotEqualTo(country3);
        assertThat(country1.hashCode()).isEqualTo(country2.hashCode());
    }

    @Test
    @DisplayName("toString() 메서드 테스트")
    void toStringTest() {
        // given
        Country country = Country.create("KOR", "대한민국", "South Korea");

        // when
        String result = country.toString();

        // then
        assertThat(result).contains("KOR", "대한민국", "South Korea");
    }
}