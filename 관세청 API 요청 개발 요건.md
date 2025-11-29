# 관세청 API 요청 개발 요건

## 프로젝트 개요
- **목적**: 관세청 공공 API를 통해 IT 부품 수입/수출 데이터 수집
- **실행 주기**: 월 1회 배치 작업
- **기술 스택**: Spring Batch + Circuit Breaker + Exponential Backoff

---

## 현재 프로젝트 상태

### 기술 스택
- Spring Boot 3.2.0, Java 17
- PostgreSQL + Redis
- DDD 아키텍처
- JPA + Hibernate

### 기존 도메인 모델
- `Country`: 국가 정보
- `ProductCategory`: HS코드 기반 품목 분류
- `ExportStatistic`: 수출 통계 (국가별, 품목별, 년월별)

---

## API 규격 분석

### 요청 파라미터
- `serviceKey`: 공공데이터 API 인증키 (필수)
- `strtYymm`: 시작년월 (필수)
- `endYymm`: 종료년월 (필수)
- `hsSgn`: 품목코드 (선택)

### 응답 필드
- `balPayments`: 무역수지
- `expDlr`: 수출금액(달러)
- `expWgt`: 수출중량(KG)
- `impDlr`: 수입금액(달러)
- `impWgt`: 수입중량(KG)
- `hsCode`: HS코드
- `statKor`: 품목명
- `year`: 기간

---

## 주요 이슈 및 결정 필요사항

### 1. Country 데이터 불일치 (중요)

**현재 시스템:**
- `ExportStatistic`은 **"한국이 어느 나라로 수출했는지"** 추적
- Country는 **수출 대상국** (예: 중국, 미국, 베트남)
- 주요 기능:
  - 국가별 수출 성과 분석
  - 지역별 집중도 분석
  - 국가별 대시보드

**관세청 API 특성:**
- 관세청 API는 **한국의 전체 수출 통계만 제공** (국가별 구분 없음)
- 국가 필터링 파라미터 없음 확인됨
- 품목별로 "전 세계에 얼마나 수출했는지"만 알 수 있음

**예시:**
```json
{
  "hsCode": "8542",
  "statKor": "전자집적회로",
  "expDlr": 15000000000,  // 전 세계 합계
  "impDlr": 3000000000,
  "year": "202301"
}
```

**선택된 해결 방안: WORLD 국가 생성**

```sql
-- V3__Add_world_country.sql
INSERT INTO countries (country_code, country_name_ko, country_name_en, region, continent)
VALUES ('WLD', '전체', 'World', '전체', '전체');
```

**배치 작업에서 사용:**
```java
Country worldCountry = countryRepository.findByCountryCode("WLD");
ExportStatistic.create(worldCountry, productCategory, period, exportValue);
```

**장점:**
- 기존 코드 변경 최소화
- 기존 대시보드에서 "전체" 필터로 조회 가능
- 테이블 구조 유지

**⚠️ TODO: 추후 국가별 수출 데이터 확인 가능 시 변경**
- 관세청에서 국가별 API 제공 시 Country 매핑 로직 추가
- 또는 다른 데이터 소스(무역협회 API 등) 활용 검토
- `data_source` 컬럼 추가하여 데이터 출처 구분 권장

### 2. 수입 데이터 처리
**문제점:**
- API는 수입(import) 데이터도 포함하지만, 현재 엔티티는 수출만 처리

**해결 방안:**
```sql
ALTER TABLE export_statistics
  ADD COLUMN import_value_usd DECIMAL(15,2),
  ADD COLUMN import_weight_kg DECIMAL(15,3),
  ADD COLUMN balance_of_payments DECIMAL(15,2),
  ADD COLUMN data_source VARCHAR(20) DEFAULT 'MANUAL'; -- 'MANUAL' or 'CUSTOMS_API'
```

### 3. 누락된 의존성
**추가 필요 라이브러리:**
```xml
- spring-boot-starter-batch
- resilience4j-spring-boot3
- spring-cloud-starter-openfeign
- poi-ooxml (Excel 처리)
- spring-boot-starter-quartz
```

### 4. DB 스키마 변경
**필요 작업:**
- Spring Batch 메타데이터 테이블 생성
- WORLD Country 레코드 추가
- 수입 데이터 필드 추가 (import_value_usd, import_weight_kg)
- 무역수지(balPayments) 필드 추가
- 데이터 출처 구분 컬럼 추가 (data_source)

**마이그레이션 파일:**
```sql
-- V3__Add_world_country_and_import_fields.sql
-- 1. WORLD Country 추가
INSERT INTO countries (country_code, country_name_ko, country_name_en, region, continent)
VALUES ('WLD', '전체', 'World', '전체', '전체');

-- 2. 수입 데이터 필드 추가
ALTER TABLE export_statistics
  ADD COLUMN import_value_usd DECIMAL(15,2),
  ADD COLUMN import_weight_kg DECIMAL(15,3),
  ADD COLUMN balance_of_payments DECIMAL(15,2),
  ADD COLUMN data_source VARCHAR(20) DEFAULT 'MANUAL';

-- 3. 인덱스 추가
CREATE INDEX idx_export_stats_data_source ON export_statistics(data_source);
```

---

## 개발 계획 (6 Phase)

### Phase 1: 기본 구조 (1-2일)
- pom.xml 의존성 추가
- DB 스키마 마이그레이션
- application.yml 설정 (Batch, Circuit Breaker)
- Batch 패키지 구조 생성

### Phase 2: Excel 처리 (1일)
- IT Product Code Load Tasklet
- Excel Reader 구현
- ItProductFilterService

### Phase 3: API 연동 (2-3일)
- CustomsApiService (Feign Client)
- Circuit Breaker 패턴 구현
- CustomsApiItemReader
- Rate Limiter 구현
- Exponential Backoff 재시도 로직
- 에러 핸들링

### Phase 4: 데이터 변환 (2일)
- CustomsDataProcessor
- Country/ProductCategory 매핑 로직
- ExportStatisticWriter (UPSERT)

### Phase 5: 스케줄링 & 모니터링 (1일)
- Quartz Scheduler 설정
- Job/Step Listener
- 로깅 & 알림

### Phase 6: 테스트 (2일)
- 단위 테스트
- 통합 테스트
- API 호출 제한 테스트

**총 예상 기간**: 9-11일

---

## 배치 작업 설계

### Job/Step 구조
```
CustomsDataSyncJob
├─ Step 1: Load IT Product Codes (Excel → Memory)
├─ Step 2: API 호출 및 데이터 수집 (Chunk-based)
│   ├─ ItemReader: CustomsApiItemReader
│   ├─ ItemProcessor: CustomsDataProcessor
│   └─ ItemWriter: TradeStatisticWriter (UPSERT)
└─ Step 3: 데이터 검증 및 알림
```

### Circuit Breaker 전략
- 실패율 임계값: 50%
- Circuit Open 시 fallback 처리
- Exponential Backoff: 1초 → 2초 → 4초 → 8초

### Rate Limiting
- API 호출 제한 고려 (예: 초당 10건)
- Chunk size 조정으로 제어
- 필요 시 Sleep 추가

---

## 확인 필요사항

### API 관련
- [x] 국가별 필터링 파라미터 존재 여부 → **없음 (전체 통계만 제공)**
- [ ] API 호출 제한 (QPS, 일일 한도)
- [ ] `year` 필드 정확한 형식 (YYYY vs YYYYMM)
- [ ] 페이징 지원 여부
- [ ] API 인증키 발급 상태
- [ ] API 전체 엔드포인트 URL

### 비즈니스 요구사항
- [ ] IT 부품 HS코드 Excel 파일 위치 및 형식
- [ ] IT 부품 범위 (HS 레벨: 2자리, 4자리, 6자리?)
- [ ] 배치 실행 시점 (매월 1일? 말일? 시간대?)
- [ ] 과거 데이터 재동기화 필요 여부
- [x] 수입 데이터 저장 여부 → **저장 (import_value_usd, import_weight_kg)**

### 설계 결정
- [x] Country 관리 방식 → **WORLD Country('WLD') 생성**
- [x] 수입/수출 데이터 관리 → **기존 테이블 확장 (컬럼 추가)**
- [ ] 실패 시 알림 방식 (이메일, Slack, 로그)
- [ ] 데이터 검증 규칙 (금액/중량 범위, 이상치 탐지)

---

## 다음 단계 액션

1. **API 문서 상세 확인**
   - 관세청 API 전체 엔드포인트 URL 확인
   - 호출 제한, 페이징, 인증 방식 파악
   - `year` 필드 형식 확인

2. **IT 부품 Excel 파일 확보**
   - 파일 위치 및 형식 확인
   - HS코드 레벨 및 범위 파악

3. **DB 마이그레이션**
   - V3__Add_world_country_and_import_fields.sql 작성
   - WORLD Country 추가
   - 수입 데이터 컬럼 추가

4. **Phase 1 시작**
   - pom.xml 의존성 추가
   - application.yml 설정 (Spring Batch, Circuit Breaker)
   - Batch 패키지 구조 생성

---

## 중요 참고사항

**⚠️ Country 데이터 제약사항:**
- 현재 API는 국가별 구분이 없으므로 **WORLD('WLD') Country로 통합 저장**
- 추후 국가별 데이터를 확보하면 다음 작업 필요:
  1. 새로운 API 또는 데이터 소스 연동
  2. Country 매핑 로직 추가
  3. `data_source` 컬럼으로 데이터 출처 구분
  4. 대시보드에서 "수동 입력 데이터 vs API 데이터" 필터링 기능

---

**작성일**: 2025-11-02
**최종 수정**: 2025-11-02
**작성자**: Claude Code
