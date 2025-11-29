-- 국가 정보 테이블
CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(3) NOT NULL UNIQUE, -- ISO 3166-1 alpha-3 코드 (예: KOR, USA, CHN)
    country_name_ko VARCHAR(100) NOT NULL,   -- 한글 국가명
    country_name_en VARCHAR(100) NOT NULL,   -- 영문 국가명
    region VARCHAR(50),                      -- 지역 (아시아, 유럽, 북미 등)
    continent VARCHAR(30),                   -- 대륙
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 품목 분류 테이블 (HS코드 기반)
CREATE TABLE product_categories (
    id BIGSERIAL PRIMARY KEY,
    hs_code VARCHAR(10) NOT NULL UNIQUE,     -- HS코드 (최대 10자리)
    hs_level INTEGER NOT NULL,               -- HS코드 레벨 (2, 4, 6, 8, 10자리)
    category_name_ko VARCHAR(200) NOT NULL,  -- 한글 품목명
    category_name_en VARCHAR(200) NOT NULL,  -- 영문 품목명
    parent_hs_code VARCHAR(10),             -- 상위 HS코드
    description TEXT,                        -- 품목 설명
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 수출 통계 데이터 테이블
CREATE TABLE export_statistics (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL,
    product_category_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,                  -- 1-12
    export_value_usd DECIMAL(15,2) NOT NULL, -- 수출액 (USD)
    export_weight_kg DECIMAL(15,3),         -- 수출 중량 (kg)
    export_quantity DECIMAL(15,3),          -- 수출 수량
    quantity_unit VARCHAR(20),              -- 수량 단위 (개, 대, 톤 등)
    growth_rate_yoy DECIMAL(5,2),           -- 전년 동기 대비 증가율 (%)
    market_share DECIMAL(5,2),              -- 해당 국가 내 시장점유율 (%)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_export_country FOREIGN KEY (country_id) REFERENCES countries(id),
    CONSTRAINT fk_export_product FOREIGN KEY (product_category_id) REFERENCES product_categories(id),
    CONSTRAINT uk_export_stats UNIQUE (country_id, product_category_id, year, month)
);

-- 환율 정보 테이블
CREATE TABLE exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) NOT NULL,       -- 통화 코드 (USD, EUR, JPY 등)
    base_currency VARCHAR(3) DEFAULT 'KRW',  -- 기준 통화
    exchange_rate DECIMAL(10,4) NOT NULL,    -- 환율
    rate_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_exchange_rate UNIQUE (currency_code, base_currency, rate_date)
);

-- 인덱스 생성
CREATE INDEX idx_export_stats_country_year ON export_statistics(country_id, year);
CREATE INDEX idx_export_stats_product_year ON export_statistics(product_category_id, year);
CREATE INDEX idx_export_stats_year_month ON export_statistics(year, month);
CREATE INDEX idx_export_stats_value ON export_statistics(export_value_usd);
CREATE INDEX idx_countries_code ON countries(country_code);
CREATE INDEX idx_product_categories_hs ON product_categories(hs_code);
CREATE INDEX idx_product_categories_level ON product_categories(hs_level);
CREATE INDEX idx_exchange_rates_date ON exchange_rates(rate_date);

-- 트리거 함수: updated_at 자동 업데이트
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 트리거 생성
CREATE TRIGGER update_countries_updated_at BEFORE UPDATE ON countries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_product_categories_updated_at BEFORE UPDATE ON product_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_export_statistics_updated_at BEFORE UPDATE ON export_statistics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();