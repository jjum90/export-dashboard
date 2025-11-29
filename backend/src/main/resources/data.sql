-- =====================================================
-- Export Dashboard - Comprehensive Dummy Data
-- =====================================================
-- This file contains realistic test data for the export dashboard system
-- with properly interconnected relationships across all entities.
--
-- Entity Relationships:
-- 1. countries (independent)
-- 2. product_categories (independent, with parent-child hierarchy)
-- 3. export_statistics (depends on countries and product_categories)
--
-- =====================================================

-- =====================================================
-- 1. COUNTRIES DATA
-- =====================================================
-- Inserting 20 realistic countries across different continents and regions
-- following ISO 3166-1 alpha-3 standard

INSERT INTO countries (country_code, country_name_ko, country_name_en, region, continent, is_active, version) VALUES
-- Asia
('CHN', '중국', 'China', 'East Asia', 'Asia', true, 0),
('JPN', '일본', 'Japan', 'East Asia', 'Asia', true, 0),
('IND', '인도', 'India', 'South Asia', 'Asia', true, 0),
('VNM', '베트남', 'Vietnam', 'Southeast Asia', 'Asia', true, 0),
('THA', '태국', 'Thailand', 'Southeast Asia', 'Asia', true, 0),
('SGP', '싱가포르', 'Singapore', 'Southeast Asia', 'Asia', true, 0),
('MYS', '말레이시아', 'Malaysia', 'Southeast Asia', 'Asia', true, 0),

-- Americas
('USA', '미국', 'United States', 'North America', 'Americas', true, 0),
('CAN', '캐나다', 'Canada', 'North America', 'Americas', true, 0),
('MEX', '멕시코', 'Mexico', 'North America', 'Americas', true, 0),
('BRA', '브라질', 'Brazil', 'South America', 'Americas', true, 0),

-- Europe
('DEU', '독일', 'Germany', 'Western Europe', 'Europe', true, 0),
('GBR', '영국', 'United Kingdom', 'Western Europe', 'Europe', true, 0),
('FRA', '프랑스', 'France', 'Western Europe', 'Europe', true, 0),
('ITA', '이탈리아', 'Italy', 'Southern Europe', 'Europe', true, 0),
('NLD', '네덜란드', 'Netherlands', 'Western Europe', 'Europe', true, 0),

-- Middle East
('SAU', '사우디아라비아', 'Saudi Arabia', 'Middle East', 'Asia', true, 0),
('ARE', '아랍에미리트', 'United Arab Emirates', 'Middle East', 'Asia', true, 0),

-- Oceania
('AUS', '호주', 'Australia', 'Oceania', 'Oceania', true, 0),
('NZL', '뉴질랜드', 'New Zealand', 'Oceania', 'Oceania', true, 0);

-- =====================================================
-- 2. PRODUCT CATEGORIES DATA
-- =====================================================
-- Inserting hierarchical product categories based on HS Code system
-- Level 1: Chapters (2 digits)
-- Level 2: Headings (4 digits)
-- Level 3: Subheadings (6 digits)

-- Chapter 84: Nuclear reactors, boilers, machinery and mechanical appliances
INSERT INTO product_categories (hs_code, hs_level, category_name_ko, category_name_en, parent_hs_code, is_active, version) VALUES
('84', 1, '원자로, 보일러, 기계류 및 기계 부품', 'Nuclear reactors, boilers, machinery and mechanical appliances', NULL, true, 0),
('8471', 2, '자동자료처리기계와 그 단위기기', 'Automatic data processing machines and units thereof', '84', true, 0),
('847130', 3, '휴대용 자동자료처리기계(무게 10kg 이하)', 'Portable automatic data processing machines, weighing not more than 10 kg', '8471', true, 0),
('847141', 3, '중앙처리장치를 포함하는 자동자료처리기계', 'Data processing machines with CPU', '8471', true, 0),

-- Chapter 85: Electrical machinery and equipment
('85', 1, '전기기기와 그 부분품', 'Electrical machinery and equipment and parts thereof', NULL, true, 0),
('8517', 2, '전화기, 송수신 기기', 'Telephone sets, including telephones for cellular networks', '85', true, 0),
('851712', 3, '휴대용 무선전화기', 'Telephones for cellular networks or for other wireless networks', '8517', true, 0),
('8528', 2, '모니터와 프로젝터, 수상기', 'Monitors and projectors, not incorporating television reception apparatus', '85', true, 0),
('852872', 3, 'LCD 수상기', 'Reception apparatus for television, whether or not incorporating radio-broadcast receivers', '8528', true, 0),

-- Chapter 87: Vehicles
('87', 1, '철도 및 궤도용 외의 차량과 그 부분품', 'Vehicles other than railway or tramway rolling-stock', NULL, true, 0),
('8703', 2, '승용자동차', 'Motor cars and other motor vehicles principally designed for the transport of persons', '87', true, 0),
('870323', 3, '왕복식 피스톤 내연기관 승용차(1500cc~3000cc)', 'Vehicles with reciprocating piston engine, 1500-3000cc', '8703', true, 0),
('870324', 3, '왕복식 피스톤 내연기관 승용차(3000cc 초과)', 'Vehicles with reciprocating piston engine, over 3000cc', '8703', true, 0),
('8708', 2, '차량 부품과 부속품', 'Parts and accessories of vehicles', '87', true, 0),
('870829', 3, '차체의 기타 부분품', 'Other parts and accessories of bodies', '8708', true, 0),

-- Chapter 39: Plastics and articles thereof
('39', 1, '플라스틱과 그 제품', 'Plastics and articles thereof', NULL, true, 0),
('3920', 2, '기타 플라스틱 판, 시트, 필름, 박', 'Other plates, sheets, film, foil and strip, of plastics', '39', true, 0),
('392010', 3, '에틸렌중합체의 플라스틱 판, 시트', 'Plates, sheets of polymers of ethylene', '3920', true, 0),

-- Chapter 29: Organic chemicals
('29', 1, '유기화학품', 'Organic chemicals', NULL, true, 0),
('2905', 2, '아실릭 알콜과 그 유도체', 'Acyclic alcohols and their derivatives', '29', true, 0),
('290545', 3, '글리세롤', 'Glycerol', '2905', true, 0),

-- Chapter 72: Iron and steel
('72', 1, '철강', 'Iron and steel', NULL, true, 0),
('7208', 2, '철 또는 비합금강 평판압연제품(폭 600mm 이상)', 'Flat-rolled products of iron or non-alloy steel, of a width of 600 mm or more', '72', true, 0),
('720851', 3, '열간압연 철강판(두께 4.75mm 이상)', 'Hot-rolled steel, not in coils, of a thickness of 4.75 mm or more', '7208', true, 0);

-- =====================================================
-- 3. EXPORT STATISTICS DATA
-- =====================================================
-- Inserting realistic export data with proper foreign key relationships
-- Data covers multiple countries, products, and time periods (2023-2025)
-- All monetary values are in USD

-- USA - Electronics exports (2024-2025)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    15500000.00, 'USD',
    125000.00, 50000.00, 'units',
    12.50, 8.75,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '847130';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    16200000.00, 'USD',
    130000.00, 52000.00, 'units',
    14.30, 9.10,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '847130';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    18500000.00, 'USD',
    145000.00, 58000.00, 'units',
    18.20, 10.50,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '847130';

-- China - Smartphones exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    45000000.00, 'USD',
    85000.00, 250000.00, 'units',
    22.50, 25.30,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CHN' AND pc.hs_code = '851712';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    48500000.00, 'USD',
    90000.00, 265000.00, 'units',
    25.80, 26.70,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CHN' AND pc.hs_code = '851712';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    52000000.00, 'USD',
    95000.00, 280000.00, 'units',
    28.50, 28.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CHN' AND pc.hs_code = '851712';

-- Japan - LCD TVs exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    28000000.00, 'USD',
    450000.00, 35000.00, 'units',
    8.30, 15.60,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'JPN' AND pc.hs_code = '852872';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    29500000.00, 'USD',
    470000.00, 37000.00, 'units',
    9.80, 16.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'JPN' AND pc.hs_code = '852872';

-- Germany - Automobiles exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    125000000.00, 'USD',
    2500000.00, 4500.00, 'units',
    15.60, 32.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870323';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    132000000.00, 'USD',
    2650000.00, 4750.00, 'units',
    17.20, 33.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870323';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    145000000.00, 'USD',
    2900000.00, 5200.00, 'units',
    19.50, 35.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870323';

-- Germany - Luxury vehicles exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    85000000.00, 'USD',
    1800000.00, 1200.00, 'units',
    12.40, 42.50,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870324';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    92000000.00, 'USD',
    1950000.00, 1300.00, 'units',
    14.80, 44.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870324';

-- Vietnam - Plastic products exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    8500000.00, 'USD',
    650000.00, 1200000.00, 'kg',
    32.50, 6.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'VNM' AND pc.hs_code = '392010';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    9200000.00, 'USD',
    700000.00, 1300000.00, 'kg',
    35.20, 7.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'VNM' AND pc.hs_code = '392010';

-- India - Organic chemicals exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    12000000.00, 'USD',
    1500000.00, 1500000.00, 'kg',
    18.50, 11.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'IND' AND pc.hs_code = '290545';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    13500000.00, 'USD',
    1650000.00, 1650000.00, 'kg',
    21.30, 12.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'IND' AND pc.hs_code = '290545';

-- Brazil - Steel products exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    22000000.00, 'USD',
    8500000.00, 8500000.00, 'kg',
    9.60, 14.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'BRA' AND pc.hs_code = '720851';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    23500000.00, 'USD',
    9000000.00, 9000000.00, 'kg',
    11.20, 15.50,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'BRA' AND pc.hs_code = '720851';

-- USA - Auto parts exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    35000000.00, 'USD',
    850000.00, 125000.00, 'units',
    16.80, 18.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '870829';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    37500000.00, 'USD',
    920000.00, 135000.00, 'units',
    18.90, 19.60,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '870829';

-- Singapore - Desktop computers exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    18500000.00, 'USD',
    280000.00, 45000.00, 'units',
    24.50, 12.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'SGP' AND pc.hs_code = '847141';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    20000000.00, 'USD',
    300000.00, 48000.00, 'units',
    27.30, 13.90,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'SGP' AND pc.hs_code = '847141';

-- Thailand - Smartphones exports (2024)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    25000000.00, 'USD',
    48000.00, 140000.00, 'units',
    28.70, 14.10,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'THA' AND pc.hs_code = '851712';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    27500000.00, 'USD',
    52000.00, 155000.00, 'units',
    31.20, 15.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'THA' AND pc.hs_code = '851712';

-- Additional data for 2023 (for year-over-year comparison)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2023, 12,
    13800000.00, 'USD',
    111000.00, 44500.00, 'units',
    10.20, 7.90,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '847130';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2023, 12,
    36700000.00, 'USD',
    69500.00, 204000.00, 'units',
    18.40, 20.60,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CHN' AND pc.hs_code = '851712';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2023, 12,
    108000000.00, 'USD',
    2160000.00, 3850.00, 'units',
    13.50, 29.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870323';

-- Recent 2025 data
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2025, 1,
    19800000.00, 'USD',
    155000.00, 62000.00, 'units',
    27.70, 11.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'USA' AND pc.hs_code = '847130';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2025, 1,
    58000000.00, 'USD',
    105000.00, 310000.00, 'units',
    28.90, 31.50,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CHN' AND pc.hs_code = '851712';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2025, 1,
    158000000.00, 'USD',
    3150000.00, 5650.00, 'units',
    26.40, 38.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'DEU' AND pc.hs_code = '870323';

-- Additional cross-country and cross-product data for comprehensive testing
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    31000000.00, 'USD',
    490000.00, 39000.00, 'units',
    11.50, 17.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'JPN' AND pc.hs_code = '852872';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    10800000.00, 'USD',
    820000.00, 1520000.00, 'kg',
    38.60, 8.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'VNM' AND pc.hs_code = '392010';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    15200000.00, 'USD',
    1850000.00, 1850000.00, 'kg',
    24.80, 13.90,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'IND' AND pc.hs_code = '290545';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 3,
    25500000.00, 'USD',
    9800000.00, 9800000.00, 'kg',
    13.60, 16.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'BRA' AND pc.hs_code = '720851';

-- France - Auto parts
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    28000000.00, 'USD',
    680000.00, 98000.00, 'units',
    14.20, 14.70,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'FRA' AND pc.hs_code = '870829';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    30500000.00, 'USD',
    735000.00, 106000.00, 'units',
    16.80, 16.00,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'FRA' AND pc.hs_code = '870829';

-- Malaysia - Laptops
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    22000000.00, 'USD',
    175000.00, 68000.00, 'units',
    19.50, 12.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'MYS' AND pc.hs_code = '847130';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    24500000.00, 'USD',
    195000.00, 75000.00, 'units',
    22.30, 13.80,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'MYS' AND pc.hs_code = '847130';

-- Canada - Steel products
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 1,
    18500000.00, 'USD',
    7200000.00, 7200000.00, 'kg',
    11.30, 12.40,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CAN' AND pc.hs_code = '720851';

INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, currency, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share, version)
SELECT
    c.id,
    pc.id,
    2024, 2,
    20000000.00, 'USD',
    7800000.00, 7800000.00, 'kg',
    13.60, 13.20,
    0
FROM countries c, product_categories pc
WHERE c.country_code = 'CAN' AND pc.hs_code = '720851';

-- Summary statistics:
-- Total records: 40+ export statistics entries
-- Covering: 13+ countries, 10+ product categories
-- Time range: 2023-12 to 2025-01 (14+ months)
-- All foreign key relationships properly maintained