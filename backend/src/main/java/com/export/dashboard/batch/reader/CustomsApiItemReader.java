package com.export.dashboard.batch.reader;

import com.export.dashboard.batch.model.CustomsApiResponse;
import com.export.dashboard.batch.service.CustomsApiService;
import com.export.dashboard.batch.service.ItProductFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

/**
 * 관세청 API로부터 무역 통계 데이터를 읽어오는 ItemReader
 */
public class CustomsApiItemReader implements ItemReader<CustomsApiResponse.TradeData> {

    private static final Logger logger = LoggerFactory.getLogger(CustomsApiItemReader.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final CustomsApiService customsApiService;
    private final ItProductFilterService itProductFilterService;
    private final String startYearMonth;
    private final String endYearMonth;

    private Iterator<CustomsApiResponse.TradeData> dataIterator;
    private boolean dataLoaded = false;

    public CustomsApiItemReader(
        CustomsApiService customsApiService,
        ItProductFilterService itProductFilterService,
        String startYearMonth,
        String endYearMonth
    ) {
        this.customsApiService = customsApiService;
        this.itProductFilterService = itProductFilterService;
        this.startYearMonth = startYearMonth;
        this.endYearMonth = endYearMonth;
    }

    @Override
    public CustomsApiResponse.TradeData read() throws Exception {
        if (!dataLoaded) {
            loadData();
            dataLoaded = true;
        }

        if (dataIterator != null && dataIterator.hasNext()) {
            return dataIterator.next();
        }

        return null; // 더 이상 읽을 데이터가 없음
    }

    /**
     * 관세청 API로부터 데이터 로드
     */
    private void loadData() {
        logger.info("관세청 API 데이터 로드 시작: {} ~ {}", startYearMonth, endYearMonth);

        List<String> itProductCodes = itProductFilterService.getAllProductCodes();
        logger.info("IT 제품 코드 {} 건에 대한 데이터 조회", itProductCodes.size());

        // 모든 IT 제품 코드에 대해 API 호출 (HS코드 파라미터 없이 전체 조회)
        // 관세청 API는 국가별 구분이 없으므로 전체 통계 조회
        CustomsApiResponse response = customsApiService.getTradeStatistics(
            startYearMonth,
            endYearMonth,
            null // hsSgn: null로 설정하여 전체 품목 조회
        );

        if (response != null && response.getTradeDataList() != null) {
            // IT 제품만 필터링
            List<CustomsApiResponse.TradeData> filteredData = response.getTradeDataList().stream()
                .filter(data -> itProductFilterService.isItProduct(data.getHsCode()))
                .toList();

            logger.info("전체 {} 건 중 IT 제품 {} 건 필터링 완료",
                response.getTradeDataList().size(), filteredData.size());

            this.dataIterator = filteredData.iterator();
        } else {
            logger.warn("관세청 API 응답이 비어있습니다");
            this.dataIterator = java.util.Collections.emptyIterator();
        }
    }

    /**
     * Reader 상태 초기화 (재시작 시 사용)
     */
    public void reset() {
        dataLoaded = false;
        dataIterator = null;
    }
}
