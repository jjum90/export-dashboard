package com.export.dashboard.batch.service;

import com.export.dashboard.batch.model.ItProductCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IT 제품 필터 서비스
 * Excel에서 로드한 IT 제품 코드 목록을 메모리에 캐시하고 필터링
 */
@Service
public class ItProductFilterService {

    private static final Logger logger = LoggerFactory.getLogger(ItProductFilterService.class);

    private final Map<String, ItProductCode> productCodeCache = new ConcurrentHashMap<>();

    /**
     * IT 제품 코드 목록을 메모리에 로드
     *
     * @param productCodes IT 제품 코드 목록
     */
    public void loadProductCodes(List<ItProductCode> productCodes) {
        productCodeCache.clear();

        for (ItProductCode productCode : productCodes) {
            productCodeCache.put(productCode.getHsCode(), productCode);
        }

        logger.info("IT 제품 코드 {} 건 메모리에 로드 완료", productCodeCache.size());
    }

    /**
     * HS코드가 IT 제품인지 확인
     *
     * @param hsCode HS코드
     * @return IT 제품 여부
     */
    public boolean isItProduct(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return false;
        }

        String normalizedCode = hsCode.trim();

        // 정확한 매칭
        if (productCodeCache.containsKey(normalizedCode)) {
            return true;
        }

        // 상위 코드 매칭 (예: 8542 -> 85, 854)
        for (int i = normalizedCode.length() - 1; i >= 2; i--) {
            String prefix = normalizedCode.substring(0, i);
            if (productCodeCache.containsKey(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * IT 제품 코드 정보 조회
     *
     * @param hsCode HS코드
     * @return IT 제품 코드 정보
     */
    public Optional<ItProductCode> getProductCode(String hsCode) {
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalizedCode = hsCode.trim();

        // 정확한 매칭
        if (productCodeCache.containsKey(normalizedCode)) {
            return Optional.of(productCodeCache.get(normalizedCode));
        }

        // 상위 코드 매칭
        for (int i = normalizedCode.length() - 1; i >= 2; i--) {
            String prefix = normalizedCode.substring(0, i);
            if (productCodeCache.containsKey(prefix)) {
                return Optional.of(productCodeCache.get(prefix));
            }
        }

        return Optional.empty();
    }

    /**
     * 모든 IT 제품 코드 목록 조회
     *
     * @return IT 제품 코드 목록
     */
    public List<String> getAllProductCodes() {
        return new ArrayList<>(productCodeCache.keySet());
    }

    /**
     * 캐시 크기 조회
     *
     * @return 캐시된 IT 제품 코드 수
     */
    public int getCacheSize() {
        return productCodeCache.size();
    }

    /**
     * 캐시 초기화
     */
    public void clearCache() {
        productCodeCache.clear();
        logger.info("IT 제품 코드 캐시 초기화 완료");
    }
}
