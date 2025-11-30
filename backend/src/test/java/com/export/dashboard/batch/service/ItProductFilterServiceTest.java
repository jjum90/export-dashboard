package com.export.dashboard.batch.service;

import com.export.dashboard.batch.model.ItProductCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ItProductFilterService 단위 테스트
 */
class ItProductFilterServiceTest {

    private ItProductFilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new ItProductFilterService();

        // 샘플 IT 제품 코드 로드
        List<ItProductCode> sampleCodes = List.of(
            new ItProductCode("8471", "자동자료처리기계", "컴퓨터 및 주변기기"),
            new ItProductCode("8542", "전자집적회로", "IC, 메모리칩"),
            new ItProductCode("8517", "전화기", "통신기기")
        );

        filterService.loadProductCodes(sampleCodes);
    }

    @Test
    @DisplayName("IT 제품 코드가 정확히 매칭되는지 확인")
    void testIsItProduct_ExactMatch() {
        // given & when & then
        assertThat(filterService.isItProduct("8471")).isTrue();
        assertThat(filterService.isItProduct("8542")).isTrue();
        assertThat(filterService.isItProduct("8517")).isTrue();
    }

    @Test
    @DisplayName("IT 제품이 아닌 코드는 false 반환")
    void testIsItProduct_NotItProduct() {
        // given & when & then
        assertThat(filterService.isItProduct("1234")).isFalse();
        assertThat(filterService.isItProduct("9999")).isFalse();
    }

    @Test
    @DisplayName("하위 코드도 IT 제품으로 인식")
    void testIsItProduct_SubCode() {
        // given & when & then
        assertThat(filterService.isItProduct("854210")).isTrue(); // 8542의 하위 코드
        assertThat(filterService.isItProduct("847101")).isTrue(); // 8471의 하위 코드
    }

    @Test
    @DisplayName("제품 코드 정보 조회")
    void testGetProductCode() {
        // given & when
        Optional<ItProductCode> product = filterService.getProductCode("8542");

        // then
        assertThat(product).isPresent();
        assertThat(product.get().getHsCode()).isEqualTo("8542");
        assertThat(product.get().getProductName()).isEqualTo("전자집적회로");
    }

    @Test
    @DisplayName("존재하지 않는 제품 코드 조회 시 빈 Optional 반환")
    void testGetProductCode_NotFound() {
        // given & when
        Optional<ItProductCode> product = filterService.getProductCode("9999");

        // then
        assertThat(product).isEmpty();
    }

    @Test
    @DisplayName("모든 제품 코드 목록 조회")
    void testGetAllProductCodes() {
        // given & when
        List<String> allCodes = filterService.getAllProductCodes();

        // then
        assertThat(allCodes).hasSize(3);
        assertThat(allCodes).contains("8471", "8542", "8517");
    }

    @Test
    @DisplayName("캐시 크기 확인")
    void testGetCacheSize() {
        // given & when
        int size = filterService.getCacheSize();

        // then
        assertThat(size).isEqualTo(3);
    }

    @Test
    @DisplayName("캐시 초기화")
    void testClearCache() {
        // given & when
        filterService.clearCache();

        // then
        assertThat(filterService.getCacheSize()).isZero();
        assertThat(filterService.isItProduct("8542")).isFalse();
    }
}
