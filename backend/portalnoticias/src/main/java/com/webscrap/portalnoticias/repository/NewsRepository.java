package com.webscrap.portalnoticias.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.webscrap.portalnoticias.entity.News;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    // Buscar por URL original (para evitar duplicados)
    Optional<News> findByOriginalUrl(String originalUrl);
    
    // Buscar noticias activas ordenadas por fecha de publicación
    Page<News> findByIsActiveTrueOrderByPublishedDateDesc(Pageable pageable);
    
    // Buscar por fuente - MEJORADO con validación de campos null
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "LOWER(TRIM(n.source)) = LOWER(TRIM(:source)) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findBySourceAndIsActiveTrueOrderByPublishedDateDesc(
        @Param("source") String source, Pageable pageable);
    
    // Buscar por categoría - MEJORADO con validación de campos null
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "n.category IS NOT NULL AND " +
           "LOWER(TRIM(n.category)) = LOWER(TRIM(:category)) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByCategoryAndIsActiveTrueOrderByPublishedDateDesc(
        @Param("category") String category, Pageable pageable);
    
    // Buscar por título (para búsqueda) - MEJORADO
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "n.title IS NOT NULL AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(n.summary, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(n.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByTitleContainingIgnoreCase(
        @Param("keyword") String keyword, Pageable pageable);
    
    // NUEVA: Búsqueda más amplia en múltiples campos
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "n.title IS NOT NULL AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(n.summary, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(n.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(n.author, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(COALESCE(n.category, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByMultipleFieldsContainingIgnoreCase(
        @Param("keyword") String keyword, Pageable pageable);
    
    // Buscar noticias recientes (últimas 24 horas) - MEJORADO
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "n.publishedDate IS NOT NULL AND " +
           "n.publishedDate >= :since " +
           "ORDER BY n.publishedDate DESC")
    List<News> findRecentNews(@Param("since") LocalDateTime since);
    
    // NUEVA: Obtener noticias recientes con límite y diversidad por fuente
    @Query(value = "SELECT * FROM (" +
           "    SELECT *, ROW_NUMBER() OVER (PARTITION BY source ORDER BY published_date DESC) as rn" +
           "    FROM news " +
           "    WHERE is_active = true AND published_date >= :since" +
           ") ranked WHERE rn <= :maxPerSource " +
           "ORDER BY published_date DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<News> findRecentNewsDiversified(
        @Param("since") LocalDateTime since, 
        @Param("maxPerSource") int maxPerSource,
        @Param("limit") int limit);
    
    // Obtener las más vistas - MEJORADO
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "n.viewCount IS NOT NULL " +
           "ORDER BY n.viewCount DESC, n.publishedDate DESC")
    Page<News> findByIsActiveTrueOrderByViewCountDescPublishedDateDesc(Pageable pageable);
    
    // NUEVA: Obtener más populares con diversidad por fuente
    @Query(value = "SELECT * FROM (" +
           "    SELECT *, ROW_NUMBER() OVER (PARTITION BY source ORDER BY view_count DESC, published_date DESC) as rn" +
           "    FROM news " +
           "    WHERE is_active = true AND view_count > 0" +
           ") ranked WHERE rn <= :maxPerSource " +
           "ORDER BY view_count DESC, published_date DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<News> findMostPopularDiversified(
        @Param("maxPerSource") int maxPerSource,
        @Param("limit") int limit);
    
    // Contar noticias por fuente - MEJORADO
    @Query("SELECT n.source, COUNT(n) FROM News n WHERE n.isActive = true " +
           "AND n.source IS NOT NULL " +
           "GROUP BY n.source ORDER BY COUNT(n) DESC")
    List<Object[]> countNewsBySource();
    
    // Obtener categorías únicas - MEJORADO
    @Query("SELECT DISTINCT TRIM(n.category) FROM News n WHERE n.isActive = true " +
           "AND n.category IS NOT NULL AND TRIM(n.category) != '' " +
           "ORDER BY TRIM(n.category)")
    List<String> findDistinctCategories();
    
    // NUEVA: Obtener fuentes únicas
    @Query("SELECT DISTINCT TRIM(n.source) FROM News n WHERE n.isActive = true " +
           "AND n.source IS NOT NULL AND TRIM(n.source) != '' " +
           "ORDER BY TRIM(n.source)")
    List<String> findDistinctSources();
    
    // Noticias por rango de fechas - MEJORADO
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "n.publishedDate IS NOT NULL AND " +
           "n.publishedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    // NUEVA: Búsqueda combinada con múltiples filtros
    @Query("SELECT n FROM News n WHERE n.isActive = true " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "    LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "    LOWER(COALESCE(n.summary, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:source IS NULL OR :source = '' OR LOWER(TRIM(n.source)) = LOWER(TRIM(:source))) " +
           "AND (:category IS NULL OR :category = '' OR LOWER(TRIM(n.category)) = LOWER(TRIM(:category))) " +
           "AND (:startDate IS NULL OR n.publishedDate >= :startDate) " +
           "AND (:endDate IS NULL OR n.publishedDate <= :endDate) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByMultipleFilters(
        @Param("keyword") String keyword,
        @Param("source") String source,
        @Param("category") String category,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    // Encontrar noticias antiguas para limpieza
    List<News> findByCreatedAtBeforeAndIsActiveTrue(LocalDateTime cutoffDate);
    
    // NUEVA: Estadísticas por fecha
    @Query("SELECT DATE(n.publishedDate) as date, COUNT(n) as count " +
           "FROM News n WHERE n.isActive = true " +
           "AND n.publishedDate >= :since " +
           "GROUP BY DATE(n.publishedDate) " +
           "ORDER BY DATE(n.publishedDate) DESC")
    List<Object[]> getNewsCountByDate(@Param("since") LocalDateTime since);
    
    // NUEVA: Top autores por cantidad de noticias
    @Query("SELECT n.author, COUNT(n) as count FROM News n " +
           "WHERE n.isActive = true AND n.author IS NOT NULL AND TRIM(n.author) != '' " +
           "GROUP BY n.author ORDER BY COUNT(n) DESC")
    List<Object[]> getTopAuthors();
    
    // NUEVA: Noticias más vistas por fuente
    @Query("SELECT n.source, MAX(n.viewCount) as maxViews, AVG(n.viewCount) as avgViews " +
           "FROM News n WHERE n.isActive = true AND n.viewCount > 0 " +
           "GROUP BY n.source ORDER BY MAX(n.viewCount) DESC")
    List<Object[]> getViewStatsBySource();
    
    // NUEVA: Verificar si existe noticia similar (para evitar duplicados por título)
    @Query("SELECT COUNT(n) > 0 FROM News n WHERE n.isActive = true " +
           "AND LOWER(TRIM(n.title)) = LOWER(TRIM(:title)) " +
           "AND n.source = :source " +
           "AND n.publishedDate >= :since")
    boolean existsSimilarNews(
        @Param("title") String title, 
        @Param("source") String source, 
        @Param("since") LocalDateTime since);
    
    // NUEVA: Obtener noticias relacionadas por categoría (excluyendo la actual)
    @Query("SELECT n FROM News n WHERE n.isActive = true " +
           "AND n.id != :excludeId " +
           "AND n.category = :category " +
           "ORDER BY n.publishedDate DESC")
    List<News> findRelatedNewsByCategory(
        @Param("excludeId") Long excludeId, 
        @Param("category") String category, 
        Pageable pageable);
    
    // NUEVA: Buscar noticias por fuente (para el service mejorado)
    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
           "LOWER(TRIM(n.source)) = LOWER(TRIM(:source)) " +
           "ORDER BY n.publishedDate DESC")
    List<News> findBySourceAndIsActiveTrueOrderByPublishedDateDesc(@Param("source") String source);
}