package com.webscrap.portalnoticias.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webscrap.portalnoticias.entity.NewsSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {
    
    /**
     * Encontrar fuentes activas
     */
    List<NewsSource> findByIsActiveTrue();
    
    /**
     * Encontrar fuente por nombre
     */
    Optional<NewsSource> findByName(String name);
    
    /**
     * NUEVA: Encontrar fuente por URL base
     */
    Optional<NewsSource> findByBaseUrl(String baseUrl);
    
    /**
     * NUEVA: Buscar fuentes por patrón de nombre (insensible a mayúsculas)
     */
    List<NewsSource> findByNameContainingIgnoreCase(String namePattern);
    
    /**
     * NUEVA: Verificar si existe fuente por nombre exacto (insensible a mayúsculas)
     */
    @Query("SELECT COUNT(ns) > 0 FROM NewsSource ns WHERE LOWER(TRIM(ns.name)) = LOWER(TRIM(:name))")
    boolean existsByNameIgnoreCase(@Param("name") String name);
    
    /**
     * NUEVA: Verificar si existe fuente por URL exacta (insensible a mayúsculas)
     */
    @Query("SELECT COUNT(ns) > 0 FROM NewsSource ns WHERE LOWER(TRIM(ns.baseUrl)) = LOWER(TRIM(:baseUrl))")
    boolean existsByBaseUrlIgnoreCase(@Param("baseUrl") String baseUrl);
    
    /**
     * Encontrar fuentes que necesitan scraping
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true AND " +
           "(ns.lastScrapedAt IS NULL OR " +
           "ns.lastScrapedAt < :cutoffTime)")
    List<NewsSource> findSourcesNeedingScraping(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * NUEVA: Encontrar fuentes que necesitan scraping con intervalo personalizado
     */
    // En el repositorio:
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true AND " +
       "(ns.lastScrapedAt IS NULL OR ns.lastScrapedAt < :cutoffTime)")
    List<NewsSource> findSourcesNeedingScrapingWithInterval(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Obtener estadísticas de fuentes
     */
    @Query("SELECT ns.name, ns.successfulScrapes, ns.failedScrapes, ns.lastScrapedAt " +
           "FROM NewsSource ns WHERE ns.isActive = true " +
           "ORDER BY ns.successfulScrapes DESC")
    List<Object[]> getSourceStatistics();
    
    /**
     * NUEVA: Obtener estadísticas completas de fuentes (incluyendo inactivas)
     */
    @Query("SELECT ns.id, ns.name, ns.isActive, ns.successfulScrapes, ns.failedScrapes, " +
           "ns.lastScrapedAt, ns.scrapingIntervalMinutes, ns.createdAt " +
           "FROM NewsSource ns ORDER BY ns.successfulScrapes DESC")
    List<Object[]> getAllSourceStatistics();
    
    /**
     * NUEVA: Contar fuentes por estado
     */
    @Query("SELECT ns.isActive, COUNT(ns) FROM NewsSource ns GROUP BY ns.isActive")
    List<Object[]> countSourcesByStatus();
    
    /**
     * NUEVA: Obtener fuentes ordenadas por éxito de scraping
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true " +
           "ORDER BY (CAST(ns.successfulScrapes AS DOUBLE) / " +
           "CASE WHEN (ns.successfulScrapes + ns.failedScrapes) = 0 THEN 1 " +
           "ELSE (ns.successfulScrapes + ns.failedScrapes) END) DESC")
    List<NewsSource> findSourcesOrderBySuccessRate();
    
    /**
     * NUEVA: Obtener fuentes que no han sido scrapeadas nunca
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true AND ns.lastScrapedAt IS NULL")
    List<NewsSource> findNeverScrapedSources();
    
    /**
     * NUEVA: Obtener fuentes scrapeadas en las últimas X horas
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true AND " +
           "ns.lastScrapedAt >= :since ORDER BY ns.lastScrapedAt DESC")
    List<NewsSource> findRecentlyScrapedSources(@Param("since") LocalDateTime since);
    
    /**
     * NUEVA: Obtener fuentes con mayor número de errores
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true AND ns.failedScrapes > 0 " +
           "ORDER BY ns.failedScrapes DESC")
    List<NewsSource> findSourcesWithMostErrors();
    
    /**
     * NUEVA: Obtener fuentes por intervalo de scraping
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.isActive = true AND " +
           "ns.scrapingIntervalMinutes = :intervalMinutes")
    List<NewsSource> findSourcesByScrapingInterval(@Param("intervalMinutes") int intervalMinutes);
    
    /**
     * NUEVA: Buscar fuentes por fragmento de URL
     */
    @Query("SELECT ns FROM NewsSource ns WHERE LOWER(ns.baseUrl) LIKE LOWER(CONCAT('%', :urlFragment, '%'))")
    List<NewsSource> findSourcesByUrlFragment(@Param("urlFragment") String urlFragment);
    
    /**
     * NUEVA: Estadísticas de rendimiento por fuente
     */
    @Query("SELECT ns.name, ns.successfulScrapes, ns.failedScrapes, " +
           "CASE WHEN (ns.successfulScrapes + ns.failedScrapes) = 0 THEN 0.0 " +
           "ELSE CAST(ns.successfulScrapes AS DOUBLE) / (ns.successfulScrapes + ns.failedScrapes) END as successRate " +
           "FROM NewsSource ns ORDER BY successRate DESC")
    List<Object[]> getSourcePerformanceStats();
    
    /**
     * NUEVA: Obtener fuentes creadas en un rango de fechas
     */
    @Query("SELECT ns FROM NewsSource ns WHERE ns.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ns.createdAt DESC")
    List<NewsSource> findSourcesCreatedBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    /**
     * NUEVA: Contar total de scrapes exitosos y fallidos
     */
    @Query("SELECT SUM(ns.successfulScrapes), SUM(ns.failedScrapes) FROM NewsSource ns WHERE ns.isActive = true")
    List<Object[]> getTotalScrapingStats();
}