package com.export.dashboard.batch.processor;

import com.export.dashboard.batch.model.CustomsApiResponse;
import com.export.dashboard.domain.model.*;
import com.export.dashboard.domain.repository.CountryRepository;
import com.export.dashboard.domain.repository.ProductCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CustomsDataProcessor 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class CustomsDataProcessorTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @InjectMocks
    private CustomsDataProcessor processor;

    private Country worldCountry;
    private ProductCategory productCategory;

    @BeforeEach
    void setUp() {
        // WORLD Country 설정
        worldCountry = Country.create("WLD", "전체", "World", "전체", "전체");

        // ProductCategory 설정
        productCategory = ProductCategory.create("8542", 4, "전자집적회로", "Integrated Circuits");
    }

    @Test
    @DisplayName("관세청 API 데이터를 ExportStatistic으로 변환")
    void testProcess_Success() throws Exception {
        // given
        CustomsApiResponse.TradeData tradeData = new CustomsApiResponse.TradeData();
        tradeData.setYear("202310");
        tradeData.setHsCode("8542");
        tradeData.setStatKor("전자집적회로");
        tradeData.setExpDlr("15000000000");
        tradeData.setExpWgt("5000000");
        tradeData.setImpDlr("3000000000");
        tradeData.setImpWgt("1000000");
        tradeData.setBalPayments("12000000000");

        when(countryRepository.findByCountryCode(any())).thenReturn(Optional.of(worldCountry));
        when(productCategoryRepository.findByHsCode(any())).thenReturn(Optional.of(productCategory));

        // when
        ExportStatistic result = processor.process(tradeData);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo(worldCountry);
        assertThat(result.getProductCategory()).isEqualTo(productCategory);
        assertThat(result.getPeriod().year()).isEqualTo(2023);
        assertThat(result.getPeriod().month()).isEqualTo(10);
        assertThat(result.getExportValue().amount()).isEqualByComparingTo(new BigDecimal("15000000000"));
        assertThat(result.getExportWeightKg()).isEqualByComparingTo(new BigDecimal("5000000"));
        assertThat(result.hasImportData()).isTrue();
        assertThat(result.isCustomsApiData()).isTrue();

        verify(countryRepository, times(1)).findByCountryCode(any());
        verify(productCategoryRepository, times(1)).findByHsCode(any());
    }

    @Test
    @DisplayName("ProductCategory가 없으면 새로 생성")
    void testProcess_CreateNewProductCategory() throws Exception {
        // given
        CustomsApiResponse.TradeData tradeData = new CustomsApiResponse.TradeData();
        tradeData.setYear("202310");
        tradeData.setHsCode("8543");
        tradeData.setStatKor("새로운 제품");
        tradeData.setExpDlr("1000000");
        tradeData.setExpWgt("100");

        when(countryRepository.findByCountryCode(any())).thenReturn(Optional.of(worldCountry));
        when(productCategoryRepository.findByHsCode(any())).thenReturn(Optional.empty());
        when(productCategoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ExportStatistic result = processor.process(tradeData);

        // then
        assertThat(result).isNotNull();
        verify(productCategoryRepository, times(1)).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("WORLD Country가 없으면 예외 발생")
    void testProcess_WorldCountryNotFound() throws Exception {
        // given
        CustomsApiResponse.TradeData tradeData = new CustomsApiResponse.TradeData();
        tradeData.setYear("202310");
        tradeData.setHsCode("8542");
        tradeData.setExpDlr("1000000");

        when(countryRepository.findByCountryCode(any())).thenReturn(Optional.empty());

        // when
        ExportStatistic result = processor.process(tradeData);

        // then - 예외가 발생해도 null 반환하여 배치 계속 진행
        assertThat(result).isNull();
    }
}
