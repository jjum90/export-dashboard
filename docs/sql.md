# Export Dashboard Database Schema Documentation

## Database Overview

The Export Dashboard uses PostgreSQL as the primary database with a well-structured schema designed to handle South Korean export statistics data efficiently. The database follows Domain-Driven Design (DDD) principles and implements proper normalization with strategic indexing for optimal performance.

### Database Information
- **Database Type**: PostgreSQL
- **Database Name**: `export_dashboard`
- **Character Encoding**: UTF-8
- **Default User**: `export_user`
- **Connection URL**: `jdbc:postgresql://localhost:5432/export_dashboard`

## Schema Overview

The database consists of 4 main tables:
1. **countries** - Master data for country information
2. **product_categories** - HS code-based product classification
3. **export_statistics** - Core export transaction data
4. **exchange_rates** - Currency exchange rate data

### Entity Relationship Diagram (Text Format)

```
countries (1) ----< (N) export_statistics (N) >---- (1) product_categories
    |                                                        |
    | id                                                     | id
    | country_code (UNIQUE)                                  | hs_code (UNIQUE)
    | country_name_ko                                        | hs_level
    | country_name_en                                        | category_name_ko
    | region                                                 | category_name_en
    | continent                                              | parent_hs_code
    | is_active                                              | description
    | created_at                                             | is_active
    | updated_at                                             | created_at
                                                             | updated_at

export_statistics:
    | id (PK)
    | country_id (FK -> countries.id)
    | product_category_id (FK -> product_categories.id)
    | year
    | month
    | export_value_usd
    | export_weight_kg
    | export_quantity
    | quantity_unit
    | growth_rate_yoy
    | market_share
    | created_at
    | updated_at

exchange_rates:
    | id (PK)
    | currency_code
    | base_currency
    | exchange_rate
    | rate_date
    | created_at
```

## Table Definitions

### 1. countries

Master table containing country information and geographic classification.

```sql
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
```

#### Column Details
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| country_code | VARCHAR(3) | NOT NULL, UNIQUE | ISO 3166-1 alpha-3 country code |
| country_name_ko | VARCHAR(100) | NOT NULL | Korean country name |
| country_name_en | VARCHAR(100) | NOT NULL | English country name |
| region | VARCHAR(50) | NULL | Geographic region classification |
| continent | VARCHAR(30) | NULL | Continent classification |
| is_active | BOOLEAN | DEFAULT true | Active status flag |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

#### Sample Data
```sql
INSERT INTO countries (country_code, country_name_ko, country_name_en, region, continent) VALUES
('CHN', '중국', 'China', '동아시아', '아시아'),
('USA', '미국', 'United States', '북미', '북미'),
('VNM', '베트남', 'Vietnam', '동남아시아', '아시아'),
('JPN', '일본', 'Japan', '동아시아', '아시아'),
('GER', '독일', 'Germany', '서유럽', '유럽');
```

### 2. product_categories

HS (Harmonized System) code-based product classification table supporting hierarchical categorization.

```sql
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
```

#### Column Details
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| hs_code | VARCHAR(10) | NOT NULL, UNIQUE | Harmonized System code (up to 10 digits) |
| hs_level | INTEGER | NOT NULL | HS code classification level (2,4,6,8,10) |
| category_name_ko | VARCHAR(200) | NOT NULL | Korean product category name |
| category_name_en | VARCHAR(200) | NOT NULL | English product category name |
| parent_hs_code | VARCHAR(10) | NULL | Parent HS code for hierarchical structure |
| description | TEXT | NULL | Detailed product description |
| is_active | BOOLEAN | DEFAULT true | Active status flag |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

#### HS Code Level Structure
- **Level 2 (2-digit)**: Chapter level (e.g., "84" - Machinery)
- **Level 4 (4-digit)**: Heading level (e.g., "8471" - Data processing machines)
- **Level 6 (6-digit)**: Subheading level (international standard)
- **Level 8/10**: National subdivision levels

#### Sample Data
```sql
INSERT INTO product_categories (hs_code, hs_level, category_name_ko, category_name_en, description) VALUES
('84', 2, '기계류 및 기계부품', 'Machinery and Mechanical Appliances', '원자로, 보일러, 기계류 및 기계부품'),
('85', 2, '전기기기 및 부품', 'Electrical Machinery and Equipment', '전기기기, 음향기기, 텔레비전기기 및 부품'),
('8471', 4, '자동자료처리기계', 'Automatic Data Processing Machines', '84'),
('8517', 4, '전화기 및 통신기기', 'Telephone Sets and Communication Equipment', '85');
```

### 3. export_statistics

Core transaction table containing detailed export statistics data with business metrics.

```sql
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
```

#### Column Details
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| country_id | BIGINT | NOT NULL, FK | Foreign key to countries table |
| product_category_id | BIGINT | NOT NULL, FK | Foreign key to product_categories table |
| year | INTEGER | NOT NULL | Export year |
| month | INTEGER | NOT NULL | Export month (1-12) |
| export_value_usd | DECIMAL(15,2) | NOT NULL | Export value in USD |
| export_weight_kg | DECIMAL(15,3) | NULL | Export weight in kilograms |
| export_quantity | DECIMAL(15,3) | NULL | Export quantity |
| quantity_unit | VARCHAR(20) | NULL | Unit of quantity measurement |
| growth_rate_yoy | DECIMAL(5,2) | NULL | Year-over-year growth rate (%) |
| market_share | DECIMAL(5,2) | NULL | Market share percentage (%) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Last update timestamp |

#### Business Rules and Constraints
- **Unique Constraint**: One record per country-product-year-month combination
- **Value Precision**: Export values support up to 15 digits with 2 decimal places
- **Weight Precision**: Export weights support 3 decimal places for precision
- **Percentage Range**: Growth rates and market shares typically range from -100.00 to 999.99

#### Sample Data
```sql
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share) VALUES
-- 중국 대상 반도체 수출
(1, 4, 2023, 1, 1250000000.00, 125000.50, 5000000, 'EA', 15.5, 25.3),
(1, 4, 2023, 2, 1180000000.00, 118000.25, 4800000, 'EA', 12.8, 24.8),
-- 미국 대상 자동차 수출
(2, 3, 2023, 1, 2100000000.00, 850000.00, 42000, 'UNIT', 8.5, 12.5),
(2, 3, 2023, 2, 1950000000.00, 780000.00, 39000, 'UNIT', 6.2, 11.8);
```

### 4. exchange_rates

Currency exchange rate data for multi-currency support and historical rate tracking.

```sql
CREATE TABLE exchange_rates (
    id BIGSERIAL PRIMARY KEY,
    currency_code VARCHAR(3) NOT NULL,       -- 통화 코드 (USD, EUR, JPY 등)
    base_currency VARCHAR(3) DEFAULT 'KRW',  -- 기준 통화
    exchange_rate DECIMAL(10,4) NOT NULL,    -- 환율
    rate_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_exchange_rate UNIQUE (currency_code, base_currency, rate_date)
);
```

#### Column Details
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-incrementing primary key |
| currency_code | VARCHAR(3) | NOT NULL | ISO 4217 currency code |
| base_currency | VARCHAR(3) | DEFAULT 'KRW' | Base currency for exchange rate |
| exchange_rate | DECIMAL(10,4) | NOT NULL | Exchange rate value |
| rate_date | DATE | NOT NULL | Date of the exchange rate |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Record creation timestamp |

#### Sample Data
```sql
INSERT INTO exchange_rates (currency_code, base_currency, exchange_rate, rate_date) VALUES
('USD', 'KRW', 1300.50, '2023-01-01'),
('USD', 'KRW', 1285.75, '2023-02-01'),
('USD', 'KRW', 1320.25, '2023-03-01');
```

## Indexes and Performance Optimization

### Primary Indexes
All tables have clustered primary key indexes on their `id` columns using BIGSERIAL.

### Secondary Indexes

#### Countries Table
```sql
CREATE INDEX idx_countries_code ON countries(country_code);
```

#### Product Categories Table
```sql
CREATE INDEX idx_product_categories_hs ON product_categories(hs_code);
CREATE INDEX idx_product_categories_level ON product_categories(hs_level);
```

#### Export Statistics Table
```sql
CREATE INDEX idx_export_stats_country_year ON export_statistics(country_id, year);
CREATE INDEX idx_export_stats_product_year ON export_statistics(product_category_id, year);
CREATE INDEX idx_export_stats_year_month ON export_statistics(year, month);
CREATE INDEX idx_export_stats_value ON export_statistics(export_value_usd);
```

#### Exchange Rates Table
```sql
CREATE INDEX idx_exchange_rates_date ON exchange_rates(rate_date);
```

### Index Strategy Rationale
- **Composite Indexes**: Support common query patterns (country+year, product+year)
- **Value Indexes**: Enable efficient range queries on export values
- **Date Indexes**: Optimize time-based queries and aggregations
- **Code Indexes**: Fast lookups on business keys (country_code, hs_code)

## Database Triggers and Functions

### Automatic Timestamp Updates

```sql
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
```

#### Trigger Functionality
- **Purpose**: Automatically updates the `updated_at` timestamp on record modifications
- **Scope**: Applies to countries, product_categories, and export_statistics tables
- **Language**: PL/pgSQL for PostgreSQL native performance

## Data Relationships and Foreign Keys

### Foreign Key Constraints

#### export_statistics Table
```sql
-- Country relationship
CONSTRAINT fk_export_country FOREIGN KEY (country_id) REFERENCES countries(id)

-- Product category relationship
CONSTRAINT fk_export_product FOREIGN KEY (product_category_id) REFERENCES product_categories(id)
```

### Referential Integrity Rules
- **ON DELETE**: Restrict (prevents deletion of referenced records)
- **ON UPDATE**: Cascade (automatically updates foreign key values)
- **Validation**: Ensures data consistency across related tables

### Relationship Cardinalities
- **countries** : **export_statistics** = 1:N (One country has many export records)
- **product_categories** : **export_statistics** = 1:N (One product category has many export records)
- **product_categories** : **product_categories** = 1:N (Self-referential hierarchy via parent_hs_code)

## Data Types and Constraints

### Precision Requirements

#### Monetary Values
- **export_value_usd**: `DECIMAL(15,2)` - Supports up to $999,999,999,999.99
- **exchange_rate**: `DECIMAL(10,4)` - Supports rates up to 999,999.9999

#### Measurement Values
- **export_weight_kg**: `DECIMAL(15,3)` - Supports weights up to 999,999,999,999.999 kg
- **export_quantity**: `DECIMAL(15,3)` - Supports quantities with 3 decimal precision

#### Percentage Values
- **growth_rate_yoy**: `DECIMAL(5,2)` - Range: -999.99% to +999.99%
- **market_share**: `DECIMAL(5,2)` - Range: 0.00% to 100.00%

### String Length Constraints
- **country_code**: 3 characters (ISO 3166-1 alpha-3)
- **hs_code**: 10 characters maximum (supports up to 10-digit HS codes)
- **quantity_unit**: 20 characters (supports various unit names)
- **Names**: 100-200 characters for multilingual support

## Query Performance Considerations

### Optimized Query Patterns

#### Dashboard Aggregations
```sql
-- Efficient yearly summary with proper indexing
SELECT
    c.country_name_en,
    SUM(es.export_value_usd) as total_export,
    AVG(es.growth_rate_yoy) as avg_growth
FROM export_statistics es
    INNER JOIN countries c ON es.country_id = c.id
WHERE es.year = 2023
    AND c.is_active = true
GROUP BY c.id, c.country_name_en
ORDER BY total_export DESC;
```

#### Time Series Analysis
```sql
-- Monthly trends with index optimization
SELECT
    year, month,
    SUM(export_value_usd) as monthly_total
FROM export_statistics
WHERE year BETWEEN 2022 AND 2023
    AND country_id = 1
GROUP BY year, month
ORDER BY year, month;
```

### Query Optimization Tips
1. **Use composite indexes** for multi-column WHERE clauses
2. **Avoid SELECT *** in production queries
3. **Use LIMIT** for pagination queries
4. **Leverage covering indexes** for frequently accessed columns
5. **Use EXPLAIN ANALYZE** to monitor query performance

## Data Migration and Versioning

### Migration Files
The database uses Flyway-style versioned migrations:

#### V1__Create_export_tables.sql
- Creates all base tables
- Establishes foreign key relationships
- Creates performance indexes
- Sets up triggers and functions

#### V2__Insert_sample_data.sql
- Inserts sample country data
- Populates product categories with HS codes
- Creates sample export statistics
- Adds exchange rate history

### Migration Best Practices
1. **Sequential numbering**: V1, V2, V3... for proper ordering
2. **Descriptive names**: Clear indication of changes made
3. **Rollback consideration**: Include rollback scripts when possible
4. **Data preservation**: Ensure existing data integrity during schema changes

## Security and Access Control

### Database Security Features

#### Connection Security
- **SSL/TLS encryption** for data in transit
- **Role-based access control** for database users
- **Connection pooling** with proper credential management

#### Data Protection
- **Column-level security** for sensitive fields
- **Audit logging** for data modification tracking
- **Backup encryption** for data at rest

### Recommended Security Practices
1. **Use environment variables** for database credentials
2. **Implement read-only users** for reporting applications
3. **Regular security updates** for PostgreSQL server
4. **Network isolation** using private subnets
5. **Regular backups** with encrypted storage

## Monitoring and Maintenance

### Performance Monitoring
- **Query execution time** tracking
- **Index usage statistics** analysis
- **Connection pool metrics** monitoring
- **Disk space utilization** alerts

### Maintenance Operations
- **Regular VACUUM** for table optimization
- **Index rebuilding** for performance maintenance
- **Statistics updates** for query planner optimization
- **Log rotation** for disk space management

### Database Metrics to Monitor
1. **Average query response time**
2. **Database connection count**
3. **Cache hit ratio**
4. **Index scan vs. sequential scan ratio**
5. **Table bloat percentage**

## Backup and Disaster Recovery

### Backup Strategy
- **Daily full backups** with point-in-time recovery
- **Transaction log backups** every 15 minutes
- **Monthly archive backups** for long-term retention

### Recovery Procedures
1. **Point-in-time recovery** for data corruption
2. **Full database restore** for complete system failures
3. **Selective table recovery** for targeted data loss
4. **Cross-region replication** for disaster recovery

## Future Schema Enhancements

### Planned Improvements
1. **Partitioning**: Implement table partitioning for export_statistics by year
2. **Materialized Views**: Create pre-computed aggregation tables
3. **Full-Text Search**: Add search capabilities for product descriptions
4. **Audit Tables**: Implement comprehensive audit logging
5. **Data Archiving**: Move old data to archive tables for performance

### Scalability Considerations
- **Horizontal partitioning** for large data volumes
- **Read replicas** for query load distribution
- **Connection pooling** optimization
- **Query result caching** implementation
- **Data compression** for storage optimization