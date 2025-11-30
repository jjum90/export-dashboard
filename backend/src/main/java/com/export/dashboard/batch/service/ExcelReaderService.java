package com.export.dashboard.batch.service;

import com.export.dashboard.batch.exception.ExcelProcessingException;
import com.export.dashboard.batch.model.ItProductCode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 파일 읽기 서비스
 * IT 부품 HS코드 목록을 Excel 파일에서 로드
 */
@Service
public class ExcelReaderService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReaderService.class);

    private final ResourceLoader resourceLoader;

    public ExcelReaderService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Excel 파일에서 IT 제품 코드 목록 로드
     *
     * @param filePath Excel 파일 경로
     * @return IT 제품 코드 목록
     */
    public List<ItProductCode> loadItProductCodes(String filePath) {
        logger.info("Excel 파일에서 IT 제품 코드 로드 시작: {}", filePath);

        try {
            Resource resource = resourceLoader.getResource(filePath);

            if (!resource.exists()) {
                throw new ExcelProcessingException("Excel 파일을 찾을 수 없습니다: " + filePath);
            }

            try (InputStream inputStream = resource.getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {

                Sheet sheet = workbook.getSheetAt(0);
                List<ItProductCode> productCodes = new ArrayList<>();

                // 헤더 행 건너뛰기 (첫 번째 행)
                int startRow = 1;
                int rowCount = 0;

                for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    try {
                        ItProductCode productCode = parseRow(row);
                        if (productCode != null) {
                            productCodes.add(productCode);
                            rowCount++;
                        }
                    } catch (Exception e) {
                        logger.warn("행 {} 파싱 실패: {}", i + 1, e.getMessage());
                    }
                }

                logger.info("Excel 파일에서 IT 제품 코드 {} 건 로드 완료", rowCount);
                return productCodes;

            } catch (IOException e) {
                throw new ExcelProcessingException("Excel 파일 읽기 실패: " + e.getMessage(), e);
            }

        } catch (IOException e) {
            throw new ExcelProcessingException("Excel 파일 리소스 로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Excel 행을 ItProductCode 객체로 변환
     *
     * @param row Excel 행
     * @return ItProductCode 객체
     */
    private ItProductCode parseRow(Row row) {
        // 컬럼 인덱스: 0=HS코드, 1=제품명, 2=설명
        String hsCode = getCellValueAsString(row.getCell(0));
        String productName = getCellValueAsString(row.getCell(1));
        String description = getCellValueAsString(row.getCell(2));

        // HS코드가 없으면 건너뛰기
        if (hsCode == null || hsCode.trim().isEmpty()) {
            return null;
        }

        return new ItProductCode(hsCode, productName, description);
    }

    /**
     * Cell 값을 String으로 변환
     *
     * @param cell Excel 셀
     * @return 셀 값 (문자열)
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 숫자를 문자열로 변환 (HS코드가 숫자로 저장된 경우)
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
}
