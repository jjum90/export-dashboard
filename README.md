# 대한민국 수출 통계 대시보드

## 프로젝트 개요
대한민국의 수출 통계 데이터를 시각화하는 웹 대시보드 프로젝트입니다.

## 기술 스택

### 백엔드
- Spring Boot 3
- Java 21
- PostgreSQL
- Redis

### 프론트엔드
- Vue 3
- TypeScript
- TailwindCSS
- Recharts

## 프로젝트 구조
```
export-dashboard/
├── backend/          # Spring Boot 애플리케이션
├── frontend/         # Vue 3 애플리케이션
├── docker/          # Docker 설정 파일들
├── docs/            # 프로젝트 문서
└── README.md
```

## 실행 방법

### 🚀 빠른 시작 (권장)

#### 프로덕션 환경
```bash
# 전체 스택을 Docker로 실행
./scripts/start-prod.sh
```

#### 개발 환경
```bash
# 데이터베이스만 Docker로 실행하고 앱은 로컬에서 실행
./scripts/start-dev.sh
```

### 🔧 수동 실행

#### 1. 데이터베이스 설정
```bash
# 개발용 PostgreSQL & Redis 시작
docker-compose -f docker/docker-compose.dev.yml up -d
```

#### 2. 백엔드 실행
```bash
cd backend
./mvnw spring-boot:run
```

#### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

### 🐳 Docker Compose 실행
```bash
# 전체 애플리케이션 스택 실행
docker-compose up -d

# 특정 서비스만 실행
docker-compose up postgres redis

# 로그 확인
docker-compose logs -f backend

# 종료
docker-compose down
```

## 🌐 접속 정보

### 프로덕션
- 프론트엔드: http://localhost:3000
- 백엔드 API: http://localhost:8080/api
- 헬스체크: http://localhost:8080/api/actuator/health

### 개발환경
- 프론트엔드: http://localhost:3000
- 백엔드 API: http://localhost:8080/api
- PostgreSQL: localhost:5433
- Redis: localhost:6380

### 데이터베이스 정보
- **사용자명**: export_user
- **비밀번호**: export_password
- **데이터베이스**: export_dashboard

## 📋 주요 API 엔드포인트

### 대시보드
- `GET /api/export-statistics/dashboard/{year}` - 연도별 대시보드 요약
- `GET /api/export-statistics/years` - 이용 가능한 연도 목록

### 국가 정보
- `GET /api/countries` - 모든 활성 국가 목록
- `GET /api/countries/regions` - 지역 목록
- `GET /api/countries/search?keyword={keyword}` - 국가 검색

### 품목 정보
- `GET /api/product-categories` - 모든 활성 품목 목록
- `GET /api/product-categories/main` - 주요 카테고리 (2자리 HS코드)
- `GET /api/product-categories/search?keyword={keyword}` - 품목 검색

### 수출 통계
- `GET /api/export-statistics/year/{year}` - 연도별 수출 통계
- `GET /api/export-statistics/country/{countryId}/year/{year}` - 국가별 연도 통계
- `GET /api/export-statistics/trend?startYear={start}&endYear={end}` - 연도별 추이