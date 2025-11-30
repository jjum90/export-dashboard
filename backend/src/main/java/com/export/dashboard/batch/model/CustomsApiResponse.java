package com.export.dashboard.batch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 관세청 API 응답 모델
 */
public class CustomsApiResponse {

    @JsonProperty("TexpimpMtYyQy")
    private List<TradeData> tradeDataList;

    public List<TradeData> getTradeDataList() {
        return tradeDataList;
    }

    public void setTradeDataList(List<TradeData> tradeDataList) {
        this.tradeDataList = tradeDataList;
    }

    /**
     * 무역 데이터 (관세청 API 응답 필드)
     */
    public static class TradeData {

        @JsonProperty("year")
        private String year; // 기간 (YYYYMM)

        @JsonProperty("hsCode")
        private String hsCode; // HS코드

        @JsonProperty("statKor")
        private String statKor; // 품목명 (한글)

        @JsonProperty("expDlr")
        private String expDlr; // 수출금액(달러)

        @JsonProperty("expWgt")
        private String expWgt; // 수출중량(KG)

        @JsonProperty("impDlr")
        private String impDlr; // 수입금액(달러)

        @JsonProperty("impWgt")
        private String impWgt; // 수입중량(KG)

        @JsonProperty("balPayments")
        private String balPayments; // 무역수지

        // Getters and Setters
        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getHsCode() {
            return hsCode;
        }

        public void setHsCode(String hsCode) {
            this.hsCode = hsCode;
        }

        public String getStatKor() {
            return statKor;
        }

        public void setStatKor(String statKor) {
            this.statKor = statKor;
        }

        public String getExpDlr() {
            return expDlr;
        }

        public void setExpDlr(String expDlr) {
            this.expDlr = expDlr;
        }

        public String getExpWgt() {
            return expWgt;
        }

        public void setExpWgt(String expWgt) {
            this.expWgt = expWgt;
        }

        public String getImpDlr() {
            return impDlr;
        }

        public void setImpDlr(String impDlr) {
            this.impDlr = impDlr;
        }

        public String getImpWgt() {
            return impWgt;
        }

        public void setImpWgt(String impWgt) {
            this.impWgt = impWgt;
        }

        public String getBalPayments() {
            return balPayments;
        }

        public void setBalPayments(String balPayments) {
            this.balPayments = balPayments;
        }

        /**
         * 수출금액을 BigDecimal로 변환
         */
        public BigDecimal getExpDlrAsBigDecimal() {
            return parseBigDecimal(expDlr);
        }

        /**
         * 수출중량을 BigDecimal로 변환
         */
        public BigDecimal getExpWgtAsBigDecimal() {
            return parseBigDecimal(expWgt);
        }

        /**
         * 수입금액을 BigDecimal로 변환
         */
        public BigDecimal getImpDlrAsBigDecimal() {
            return parseBigDecimal(impDlr);
        }

        /**
         * 수입중량을 BigDecimal로 변환
         */
        public BigDecimal getImpWgtAsBigDecimal() {
            return parseBigDecimal(impWgt);
        }

        /**
         * 무역수지를 BigDecimal로 변환
         */
        public BigDecimal getBalPaymentsAsBigDecimal() {
            return parseBigDecimal(balPayments);
        }

        private BigDecimal parseBigDecimal(String value) {
            if (value == null || value.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            try {
                // 쉼표 제거 후 변환
                String cleaned = value.replaceAll(",", "").trim();
                return new BigDecimal(cleaned);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        @Override
        public String toString() {
            return String.format("TradeData[year=%s, hsCode=%s, statKor=%s, expDlr=%s, impDlr=%s]",
                year, hsCode, statKor, expDlr, impDlr);
        }
    }
}
