-- 주요 국가 데이터 삽입
INSERT INTO countries (country_code, country_name_ko, country_name_en, region, continent) VALUES
('CHN', '중국', 'China', '동아시아', '아시아'),
('USA', '미국', 'United States', '북미', '북미'),
('VNM', '베트남', 'Vietnam', '동남아시아', '아시아'),
('JPN', '일본', 'Japan', '동아시아', '아시아'),
('IND', '인도', 'India', '남아시아', '아시아'),
('GER', '독일', 'Germany', '서유럽', '유럽'),
('SGP', '싱가포르', 'Singapore', '동남아시아', '아시아'),
('THA', '태국', 'Thailand', '동남아시아', '아시아'),
('TWN', '대만', 'Taiwan', '동아시아', '아시아'),
('MYS', '말레이시아', 'Malaysia', '동남아시아', '아시아'),
('IDN', '인도네시아', 'Indonesia', '동남아시아', '아시아'),
('HKG', '홍콩', 'Hong Kong', '동아시아', '아시아'),
('MEX', '멕시코', 'Mexico', '중미', '북미'),
('TUR', '터키', 'Turkey', '서아시아', '아시아'),
('AUS', '호주', 'Australia', '오세아니아', '오세아니아');

-- 주요 품목 카테고리 데이터 삽입 (HS코드 2자리)
INSERT INTO product_categories (hs_code, hs_level, category_name_ko, category_name_en, description) VALUES
('84', 2, '기계류 및 기계부품', 'Machinery and Mechanical Appliances', '원자로, 보일러, 기계류 및 기계부품'),
('85', 2, '전기기기 및 부품', 'Electrical Machinery and Equipment', '전기기기, 음향기기, 텔레비전기기 및 부품'),
('87', 2, '자동차 및 부품', 'Vehicles and Parts', '철도 외의 차량 및 부품과 액세서리'),
('90', 2, '정밀기기', 'Optical and Precision Instruments', '광학기기, 사진기기, 영화기기, 측정기기, 정밀기기'),
('39', 2, '플라스틱 제품', 'Plastics and Articles', '플라스틱과 그 제품'),
('72', 2, '철강', 'Iron and Steel', '철과 강철'),
('29', 2, '유기화학품', 'Organic Chemicals', '유기화학품'),
('73', 2, '철강제품', 'Articles of Iron or Steel', '철강제품'),
('38', 2, '기타화학제품', 'Miscellaneous Chemical Products', '각종 화학공업 생산품'),
('40', 2, '고무제품', 'Rubber and Articles', '고무와 그 제품');

-- 4자리 HS코드 (일부 예시)
INSERT INTO product_categories (hs_code, hs_level, category_name_ko, category_name_en, parent_hs_code) VALUES
('8471', 4, '자동자료처리기계', 'Automatic Data Processing Machines', '84'),
('8517', 4, '전화기 및 통신기기', 'Telephone Sets and Communication Equipment', '85'),
('8703', 4, '승용자동차', 'Motor Cars and Vehicles', '87'),
('8542', 4, '전자집적회로', 'Electronic Integrated Circuits', '85'),
('8708', 4, '자동차부품', 'Parts and Accessories of Motor Vehicles', '87');

-- 샘플 수출 통계 데이터 (최근 2년)
INSERT INTO export_statistics (country_id, product_category_id, year, month, export_value_usd, export_weight_kg, export_quantity, quantity_unit, growth_rate_yoy, market_share) VALUES
-- 중국 대상 반도체 수출
(1, 4, 2023, 1, 1250000000.00, 125000.50, 5000000, 'EA', 15.5, 25.3),
(1, 4, 2023, 2, 1180000000.00, 118000.25, 4800000, 'EA', 12.8, 24.8),
(1, 4, 2023, 3, 1350000000.00, 135000.75, 5200000, 'EA', 18.2, 26.1),

-- 미국 대상 자동차 수출
(2, 3, 2023, 1, 2100000000.00, 850000.00, 42000, 'UNIT', 8.5, 12.5),
(2, 3, 2023, 2, 1950000000.00, 780000.00, 39000, 'UNIT', 6.2, 11.8),
(2, 3, 2023, 3, 2250000000.00, 900000.00, 45000, 'UNIT', 10.3, 13.2),

-- 베트남 대상 기계류 수출
(3, 1, 2023, 1, 450000000.00, 225000.00, 1500, 'SET', 22.1, 18.7),
(3, 1, 2023, 2, 420000000.00, 210000.00, 1400, 'SET', 19.5, 17.9),
(3, 1, 2023, 3, 480000000.00, 240000.00, 1600, 'SET', 25.3, 19.8);

-- 환율 데이터 (USD 기준)
INSERT INTO exchange_rates (currency_code, base_currency, exchange_rate, rate_date) VALUES
('USD', 'KRW', 1300.50, '2023-01-01'),
('USD', 'KRW', 1285.75, '2023-02-01'),
('USD', 'KRW', 1320.25, '2023-03-01'),
('USD', 'KRW', 1295.00, '2023-04-01'),
('USD', 'KRW', 1310.80, '2023-05-01'),
('USD', 'KRW', 1275.30, '2023-06-01');