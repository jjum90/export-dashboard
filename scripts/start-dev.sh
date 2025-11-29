#!/bin/bash

echo "🚀 대한민국 수출 통계 대시보드 개발 환경 시작"

# 개발용 Docker 컨테이너 시작
echo "📦 PostgreSQL과 Redis 컨테이너 시작 중..."
docker-compose -f docker/docker-compose.dev.yml up -d

# 컨테이너가 준비될 때까지 대기
echo "⏳ 데이터베이스가 준비될 때까지 대기 중..."
sleep 10

# 백엔드 시작
echo "🔧 Spring Boot 백엔드 시작 중..."
cd backend
./mvnw spring-boot:run &
BACKEND_PID=$!

# 프론트엔드 시작
echo "🎨 Vue.js 프론트엔드 시작 중..."
cd ../frontend
npm install
npm run dev &
FRONTEND_PID=$!

echo "✅ 개발 환경 시작 완료!"
echo "📊 프론트엔드: http://localhost:3000"
echo "🔗 백엔드 API: http://localhost:8080/api"
echo "🐘 PostgreSQL: localhost:5433"
echo "🔴 Redis: localhost:6380"
echo ""
echo "💡 종료하려면 Ctrl+C를 누르세요"

# Ctrl+C 처리
trap "echo '🛑 개발 환경 종료 중...'; kill $BACKEND_PID $FRONTEND_PID; docker-compose -f docker/docker-compose.dev.yml down; exit" INT

# 프로세스가 종료될 때까지 대기
wait