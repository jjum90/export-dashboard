-- V3: WORLD Country 추가 및 수입 데이터 필드 추가
-- 관세청 API는 국가별 구분이 없으므로 WORLD Country로 통합 저장

-- 1. WORLD Country 추가
INSERT INTO countries (country_code, country_name_ko, country_name_en, region, continent, is_active)
VALUES ('WLD', '전체', 'World', '전체', '전체', true)
ON CONFLICT (country_code) DO NOTHING;

-- 2. export_statistics 테이블에 수입 데이터 필드 추가
ALTER TABLE export_statistics
    ADD COLUMN IF NOT EXISTS import_value_usd DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS import_weight_kg DECIMAL(15,3),
    ADD COLUMN IF NOT EXISTS balance_of_payments DECIMAL(15,2),
    ADD COLUMN IF NOT EXISTS data_source VARCHAR(20) DEFAULT 'MANUAL',
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) DEFAULT 'USD',
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- 3. 데이터 소스 체크 제약조건 추가
ALTER TABLE export_statistics
    ADD CONSTRAINT chk_data_source CHECK (data_source IN ('MANUAL', 'CUSTOMS_API'));

-- 4. 인덱스 추가
CREATE INDEX IF NOT EXISTS idx_export_stats_data_source ON export_statistics(data_source);
CREATE INDEX IF NOT EXISTS idx_export_stats_period ON export_statistics(year, month, data_source);

-- 5. 기존 데이터에 대한 data_source 기본값 설정
UPDATE export_statistics
SET data_source = 'MANUAL'
WHERE data_source IS NULL;

-- 6. 코멘트 추가
COMMENT ON COLUMN export_statistics.import_value_usd IS '수입금액(달러) - 관세청 API impDlr';
COMMENT ON COLUMN export_statistics.import_weight_kg IS '수입중량(KG) - 관세청 API impWgt';
COMMENT ON COLUMN export_statistics.balance_of_payments IS '무역수지 - 관세청 API balPayments';
COMMENT ON COLUMN export_statistics.data_source IS '데이터 출처: MANUAL(수동 입력), CUSTOMS_API(관세청 API)';
COMMENT ON COLUMN export_statistics.version IS 'Optimistic locking version';
