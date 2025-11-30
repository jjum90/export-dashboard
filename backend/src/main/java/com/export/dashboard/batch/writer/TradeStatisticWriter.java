package com.export.dashboard.batch.writer;

import com.export.dashboard.domain.model.ExportStatistic;
import com.export.dashboard.domain.repository.ExportStatisticRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 무역 통계 데이터를 DB에 UPSERT하는 Writer
 * 기존 데이터가 있으면 업데이트, 없으면 삽입
 */
@Component
public class TradeStatisticWriter implements ItemWriter<ExportStatistic> {

    private static final Logger logger = LoggerFactory.getLogger(TradeStatisticWriter.class);

    private final ExportStatisticRepository exportStatisticRepository;

    public TradeStatisticWriter(ExportStatisticRepository exportStatisticRepository) {
        this.exportStatisticRepository = exportStatisticRepository;
    }

    @Override
    @Transactional
    public void write(Chunk<? extends ExportStatistic> chunk) throws Exception {
        int insertCount = 0;
        int updateCount = 0;

        for (ExportStatistic newStatistic : chunk) {
            if (newStatistic == null) {
                continue; // Processor에서 null 반환한 경우 건너뛰기
            }

            try {
                // 기존 데이터 조회 (Country, ProductCategory, Year, Month 기준)
                Optional<ExportStatistic> existingOpt = exportStatisticRepository
                    .findByCountryAndProductCategoryAndPeriod(
                        newStatistic.getCountry(),
                        newStatistic.getProductCategory(),
                        newStatistic.getPeriod().year(),
                        newStatistic.getPeriod().month()
                    );

                if (existingOpt.isPresent()) {
                    // 업데이트
                    ExportStatistic existing = existingOpt.get();
                    updateExistingStatistic(existing, newStatistic);
                    exportStatisticRepository.save(existing);
                    updateCount++;

                    logger.debug("무역 통계 업데이트: country={}, hsCode={}, period={}",
                        existing.getCountry().getCountryCode(),
                        existing.getProductCategory().getHsCode(),
                        existing.getPeriod());
                } else {
                    // 삽입
                    exportStatisticRepository.save(newStatistic);
                    insertCount++;

                    logger.debug("무역 통계 삽입: country={}, hsCode={}, period={}",
                        newStatistic.getCountry().getCountryCode(),
                        newStatistic.getProductCategory().getHsCode(),
                        newStatistic.getPeriod());
                }

            } catch (Exception e) {
                logger.error("무역 통계 저장 실패: country={}, hsCode={}, period={}, error={}",
                    newStatistic.getCountry().getCountryCode(),
                    newStatistic.getProductCategory().getHsCode(),
                    newStatistic.getPeriod(),
                    e.getMessage(), e);
                // 개별 항목 실패는 로그만 남기고 계속 진행
            }
        }

        logger.info("무역 통계 저장 완료: 삽입 {} 건, 업데이트 {} 건", insertCount, updateCount);
    }

    /**
     * 기존 통계 데이터를 새로운 데이터로 업데이트
     */
    private void updateExistingStatistic(ExportStatistic existing, ExportStatistic newStatistic) {
        // 수출 금액 업데이트
        existing.updateExportValue(newStatistic.getExportValue());

        // 수출 중량 업데이트
        if (newStatistic.getExportWeightKg() != null) {
            existing.updateWeight(newStatistic.getExportWeightKg());
        }

        // 수입 데이터 업데이트
        if (newStatistic.hasImportData()) {
            existing.updateImportData(
                newStatistic.getImportValue(),
                newStatistic.getImportWeightKg(),
                newStatistic.getBalanceOfPayments()
            );
        }

        // 데이터 출처 업데이트
        if (newStatistic.isCustomsApiData()) {
            existing.markAsCustomsApiData();
        }
    }
}
