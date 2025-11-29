#!/bin/bash

echo "🚀 대한민국 수출 통계 대시보드 프로덕션 환경 시작"

# Docker Compose로 전체 스택 시작
echo "📦 전체 애플리케이션 스택 시작 중..."
docker-compose up -d

echo "⏳ 서비스가 시작되는 동안 잠시 기다려주세요..."
sleep 30

echo "✅ 프로덕션 환경 시작 완료!"
echo "📊 애플리케이션: http://localhost:3000"
echo "🔗 백엔드 API: http://localhost:8080/api"
echo "🐘 PostgreSQL: localhost:5432"
echo "🔴 Redis: localhost:6379"
echo ""
echo "📊 서비스 상태 확인:"
docker-compose ps

echo ""
echo "📋 서비스 로그 확인: docker-compose logs -f [service-name]"
echo "🛑 서비스 종료: docker-compose down"