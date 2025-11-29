package com.export.dashboard.infrastructure.repository;

import com.export.dashboard.domain.model.Country;
import com.export.dashboard.domain.model.ExportStatistic;
import com.export.dashboard.domain.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ExportStatistic JPA Repository 인터페이스
 */
public interface JpaExportStatisticRepository extends JpaRepository<ExportStatistic, Long> {

    @Query("SELECT e FROM ExportStatistic e " +
           "WHERE e.country = :country AND e.productCategory = :productCategory " +
           "AND e.period.year = :year AND e.period.month = :month")
    Optional<ExportStatistic> findByCountryAndProductAndPeriod(
        @Param("country") Country country,
        @Param("productCategory") ProductCategory productCategory,
        @Param("year") Integer year,
        @Param("month") Integer month);

    @Query("SELECT e FROM ExportStatistic e WHERE e.period.year = :year ORDER BY e.period.month, e.exportValue.amount DESC")
    List<ExportStatistic> findByYear(@Param("year") Integer year);

    @Query("SELECT e FROM ExportStatistic e WHERE e.country = :country AND e.period.year = :year ORDER BY e.period.month, e.exportValue.amount DESC")
    List<ExportStatistic> findByCountryAndYear(@Param("country") Country country, @Param("year") Integer year);

    @Query("SELECT e FROM ExportStatistic e WHERE e.productCategory = :productCategory AND e.period.year = :year ORDER BY e.period.month, e.exportValue.amount DESC")
    List<ExportStatistic> findByProductCategoryAndYear(@Param("productCategory") ProductCategory productCategory, @Param("year") Integer year);

    @Query("SELECT e FROM ExportStatistic e " +
           "WHERE e.period.year BETWEEN :startYear AND :endYear " +
           "AND (:startYear != :endYear OR e.period.month BETWEEN :startMonth AND :endMonth) " +
           "ORDER BY e.period.year, e.period.month")
    List<ExportStatistic> findByPeriodBetween(
        @Param("startYear") Integer startYear,
        @Param("startMonth") Integer startMonth,
        @Param("endYear") Integer endYear,
        @Param("endMonth") Integer endMonth);

    @Query("SELECT COALESCE(SUM(e.exportValue.amount), 0) FROM ExportStatistic e WHERE e.period.year = :year")
    BigDecimal getTotalExportValueByYear(@Param("year") Integer year);

    @Query("SELECT COUNT(DISTINCT e.country) FROM ExportStatistic e WHERE e.period.year = :year")
    Long countDistinctCountriesByYear(@Param("year") Integer year);

    @Query("SELECT COUNT(DISTINCT e.productCategory) FROM ExportStatistic e WHERE e.period.year = :year")
    Long countDistinctProductsByYear(@Param("year") Integer year);

    @Query("SELECT e.country.countryCode.value, e.country.nameEn, SUM(e.exportValue.amount) " +
           "FROM ExportStatistic e " +
           "WHERE e.period.year = :year " +
           "GROUP BY e.country.countryCode.value, e.country.nameEn " +
           "ORDER BY SUM(e.exportValue.amount) DESC")
    List<Object[]> getTopExportCountriesByYear(@Param("year") Integer year);

    @Query("SELECT e.productCategory.hsCode.value, e.productCategory.nameEn, SUM(e.exportValue.amount) " +
           "FROM ExportStatistic e " +
           "WHERE e.period.year = :year " +
           "GROUP BY e.productCategory.hsCode.value, e.productCategory.nameEn " +
           "ORDER BY SUM(e.exportValue.amount) DESC")
    List<Object[]> getTopExportProductsByYear(@Param("year") Integer year);

    @Query("SELECT e.period.year, e.period.month, SUM(e.exportValue.amount) " +
           "FROM ExportStatistic e " +
           "WHERE e.period.year = :year " +
           "GROUP BY e.period.year, e.period.month " +
           "ORDER BY e.period.month")
    List<Object[]> getMonthlyExportTrend(@Param("year") Integer year);

    @Query("SELECT e.period.year, SUM(e.exportValue.amount) " +
           "FROM ExportStatistic e " +
           "WHERE e.period.year BETWEEN :startYear AND :endYear " +
           "GROUP BY e.period.year " +
           "ORDER BY e.period.year")
    List<Object[]> getYearlyExportTrend(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    @Query("SELECT DISTINCT e.period.year FROM ExportStatistic e ORDER BY e.period.year DESC")
    List<Integer> findAllYears();

    @Query("SELECT e FROM ExportStatistic e " +
           "WHERE e.country = :country AND e.productCategory = :productCategory " +
           "AND e.period.year = :year AND e.period.month = :month")
    List<ExportStatistic> findAllByCountryAndProductAndPeriod(
        @Param("country") Country country,
        @Param("productCategory") ProductCategory productCategory,
        @Param("year") Integer year,
        @Param("month") Integer month);

    @Query("SELECT e FROM ExportStatistic e " +
           "WHERE e.productCategory = :productCategory " +
           "AND e.period.year = :year AND e.period.month = :month")
    List<ExportStatistic> findByProductAndPeriod(
        @Param("productCategory") ProductCategory productCategory,
        @Param("year") Integer year,
        @Param("month") Integer month);

    @Query("SELECT e FROM ExportStatistic e " +
           "WHERE e.country = :country " +
           "AND e.period.year = :year AND e.period.month = :month " +
           "ORDER BY e.exportValue.amount DESC")
    List<ExportStatistic> findByCountryAndPeriodOrderByValueDesc(
        @Param("country") Country country,
        @Param("year") Integer year,
        @Param("month") Integer month);

    @Query("SELECT e FROM ExportStatistic e " +
           "WHERE e.country.region = :region " +
           "AND e.period.year = :year AND e.period.month = :month")
    List<ExportStatistic> findByRegionAndPeriod(
        @Param("region") String region,
        @Param("year") Integer year,
        @Param("month") Integer month);
}