package com.export.dashboard.batch.job;

import com.export.dashboard.batch.config.CustomsBatchProperties;
import com.export.dashboard.batch.listener.CustomsDataSyncJobListener;
import com.export.dashboard.batch.listener.CustomsDataSyncStepListener;
import com.export.dashboard.batch.model.CustomsApiResponse;
import com.export.dashboard.batch.model.ItProductCode;
import com.export.dashboard.batch.processor.CustomsDataProcessor;
import com.export.dashboard.batch.reader.CustomsApiItemReader;
import com.export.dashboard.batch.service.CustomsApiService;
import com.export.dashboard.batch.service.ExcelReaderService;
import com.export.dashboard.batch.service.ItProductFilterService;
import com.export.dashboard.batch.writer.TradeStatisticWriter;
import com.export.dashboard.domain.model.ExportStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 관세청 데이터 동기화 배치 작업 설정
 *
 * Job 구조:
 * 1. Step 1: IT 제품 코드 로드 (Excel → Memory)
 * 2. Step 2: API 호출 및 데이터 수집 (Chunk-based)
 * 3. Step 3: 데이터 검증 및 통계
 */
@Configuration
public class CustomsDataSyncJobConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CustomsDataSyncJobConfiguration.class);
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CustomsBatchProperties batchProperties;
    private final ExcelReaderService excelReaderService;
    private final ItProductFilterService itProductFilterService;
    private final CustomsApiService customsApiService;
    private final CustomsDataProcessor customsDataProcessor;
    private final TradeStatisticWriter tradeStatisticWriter;
    private final CustomsDataSyncJobListener jobListener;
    private final CustomsDataSyncStepListener stepListener;

    public CustomsDataSyncJobConfiguration(
        JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        CustomsBatchProperties batchProperties,
        ExcelReaderService excelReaderService,
        ItProductFilterService itProductFilterService,
        CustomsApiService customsApiService,
        CustomsDataProcessor customsDataProcessor,
        TradeStatisticWriter tradeStatisticWriter,
        CustomsDataSyncJobListener jobListener,
        CustomsDataSyncStepListener stepListener
    ) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.batchProperties = batchProperties;
        this.excelReaderService = excelReaderService;
        this.itProductFilterService = itProductFilterService;
        this.customsApiService = customsApiService;
        this.customsDataProcessor = customsDataProcessor;
        this.tradeStatisticWriter = tradeStatisticWriter;
        this.jobListener = jobListener;
        this.stepListener = stepListener;
    }

    /**
     * 관세청 데이터 동기화 Job
     */
    @Bean
    public Job customsDataSyncJob() {
        return new JobBuilder("customsDataSyncJob", jobRepository)
            .listener(jobListener)
            .start(loadItProductCodesStep())
            .next(syncTradeDataStep())
            .next(validateDataStep())
            .build();
    }

    /**
     * Step 1: IT 제품 코드 로드 (Excel → Memory)
     */
    @Bean
    public Step loadItProductCodesStep() {
        return new StepBuilder("loadItProductCodesStep", jobRepository)
            .tasklet(loadItProductCodesTasklet(), transactionManager)
            .listener(stepListener)
            .build();
    }

    /**
     * IT 제품 코드 로드 Tasklet
     */
    @Bean
    public Tasklet loadItProductCodesTasklet() {
        return (contribution, chunkContext) -> {
            logger.info("=== Step 1: IT 제품 코드 로드 시작 ===");

            String excelFilePath = batchProperties.getExcelFilePath();
            List<ItProductCode> productCodes = excelReaderService.loadItProductCodes(excelFilePath);

            itProductFilterService.loadProductCodes(productCodes);

            logger.info("IT 제품 코드 {} 건 메모리에 로드 완료", productCodes.size());

            // ExecutionContext에 저장 (다음 Step에서 사용)
            chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .putInt("productCodeCount", productCodes.size());

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * Step 2: API 호출 및 데이터 수집 (Chunk-based)
     */
    @Bean
    public Step syncTradeDataStep() {
        return new StepBuilder("syncTradeDataStep", jobRepository)
            .<CustomsApiResponse.TradeData, ExportStatistic>chunk(batchProperties.getChunkSize(), transactionManager)
            .reader(customsApiItemReader())
            .processor(customsDataProcessor)
            .writer(tradeStatisticWriter)
            .listener(stepListener)
            .build();
    }

    /**
     * Customs API Item Reader
     */
    @Bean
    public CustomsApiItemReader customsApiItemReader() {
        // 지난 달 데이터 조회 (기본값)
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String startYearMonth = lastMonth.format(YEAR_MONTH_FORMATTER);
        String endYearMonth = lastMonth.format(YEAR_MONTH_FORMATTER);

        return new CustomsApiItemReader(
            customsApiService,
            itProductFilterService,
            startYearMonth,
            endYearMonth
        );
    }

    /**
     * Step 3: 데이터 검증 및 통계
     */
    @Bean
    public Step validateDataStep() {
        return new StepBuilder("validateDataStep", jobRepository)
            .tasklet(validateDataTasklet(), transactionManager)
            .listener(stepListener)
            .build();
    }

    /**
     * 데이터 검증 Tasklet
     */
    @Bean
    public Tasklet validateDataTasklet() {
        return (contribution, chunkContext) -> {
            logger.info("=== Step 3: 데이터 검증 시작 ===");

            // ExecutionContext에서 통계 정보 조회
            var executionContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            int productCodeCount = executionContext.getInt("productCodeCount", 0);

            logger.info("배치 작업 완료 - IT 제품 코드: {} 건", productCodeCount);

            return RepeatStatus.FINISHED;
        };
    }
}
