package com.webscrap.portalnoticias.service;

import com.webscrap.portalnoticias.dto.*;
import com.webscrap.portalnoticias.entity.News;
import com.webscrap.portalnoticias.repository.NewsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NewsService {
    
    private final NewsRepository newsRepository;
    
    /**
     * Obtener todas las noticias con paginación
     */
    public PagedNewsResponse getAllNews(int page, int size, String sortBy, String sortDirection) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<News> newsPage = newsRepository.findByIsActiveTrueOrderByPublishedDateDesc(pageable);
            
            return convertToPagedResponse(newsPage);
        } catch (Exception e) {
            log.error("Error obteniendo todas las noticias: {}", e.getMessage());
            throw new RuntimeException("Error al obtener noticias", e);
        }
    }
    
    /**
     * Obtener noticia por ID e incrementar contador de vistas
     */
    @Transactional
    public Optional<NewsDTO> getNewsById(Long id) {
        try {
            Optional<News> newsOpt = newsRepository.findById(id);
            
            if (newsOpt.isPresent() && newsOpt.get().getIsActive()) {
                News news = newsOpt.get();
                news.incrementViewCount();
                newsRepository.save(news);
                
                return Optional.of(convertToDTO(news));
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error obteniendo noticia por ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener noticia por ID", e);
        }
    }
    
    /**
     * Buscar noticias - CORREGIDO CON VALIDACIONES
     */
    public PagedNewsResponse searchNews(NewsSearchRequest request) {
        try {
            log.debug("Procesando búsqueda: {}", request);
            
            // Validaciones
            if (request == null) {
                throw new IllegalArgumentException("Request no puede ser null");
            }
            
            // Asegurar valores por defecto
            if (request.getPage() < 0) request.setPage(0);
            if (request.getSize() <= 0) request.setSize(20);
            if (request.getSortBy() == null) request.setSortBy("publishedDate");
            if (request.getSortDirection() == null) request.setSortDirection("desc");
            
            Pageable pageable = createPageable(request);
            Page<News> newsPage = null;
            
            // Búsqueda por keyword
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                log.debug("Búsqueda por keyword: '{}'", request.getKeyword());
                newsPage = newsRepository.findByTitleContainingIgnoreCase(
                        request.getKeyword().trim(), pageable);
                        
            // Búsqueda por fuente
            } else if (request.getSource() != null && !request.getSource().trim().isEmpty()) {
                log.debug("Búsqueda por fuente: '{}'", request.getSource());
                newsPage = newsRepository.findBySourceAndIsActiveTrueOrderByPublishedDateDesc(
                        request.getSource().trim(), pageable);
                        
            // Búsqueda por categoría
            } else if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
                log.debug("Búsqueda por categoría: '{}'", request.getCategory());
                newsPage = newsRepository.findByCategoryAndIsActiveTrueOrderByPublishedDateDesc(
                        request.getCategory().trim(), pageable);
                        
            // Búsqueda por rango de fechas
            } else if (request.getStartDate() != null && request.getEndDate() != null) {
                log.debug("Búsqueda por fechas: {} - {}", request.getStartDate(), request.getEndDate());
                newsPage = newsRepository.findByDateRange(
                        request.getStartDate(), request.getEndDate(), pageable);
                        
            // Si no hay filtros específicos, obtener todas las noticias activas
            } else {
                log.debug("Búsqueda general (sin filtros)");
                newsPage = newsRepository.findByIsActiveTrueOrderByPublishedDateDesc(pageable);
            }
            
            if (newsPage == null) {
                log.warn("No se pudo ejecutar la búsqueda");
                return createEmptyResponse();
            }
            
            log.debug("Búsqueda completada: {} resultados", newsPage.getTotalElements());
            return convertToPagedResponse(newsPage);
            
        } catch (Exception e) {
            log.error("Error en búsqueda de noticias: {}", e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda de noticias", e);
        }
    }
    
    /**
     * Obtener noticias más populares con MEZCLA DE FUENTES
     */
    public PagedNewsResponse getMostPopularNewsMixed(int page, int size) {
        try {
            // Obtener más noticias para hacer la mezcla
            int expandedSize = size * 3;
            Pageable pageable = PageRequest.of(0, expandedSize);
            
            Page<News> allPopularNews = newsRepository
                    .findByIsActiveTrueOrderByViewCountDescPublishedDateDesc(pageable);
            
            List<News> mixedNews = diversifyNewsBySource(allPopularNews.getContent(), size);
            
            // Calcular paginación manual
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, mixedNews.size());
            
            List<News> pageContent = mixedNews.subList(startIndex, endIndex);
            
            return PagedNewsResponse.builder()
                    .news(pageContent.stream()
                            .map(this::convertToSummaryDTO)
                            .collect(Collectors.toList()))
                    .currentPage(page)
                    .totalPages((int) Math.ceil((double) mixedNews.size() / size))
                    .totalElements(mixedNews.size())
                    .hasNext(endIndex < mixedNews.size())
                    .hasPrevious(page > 0)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error obteniendo noticias populares: {}", e.getMessage());
            throw new RuntimeException("Error al obtener noticias populares", e);
        }
    }
    
    /**
     * Obtener noticias recientes con MEZCLA DE FUENTES
     */
    public List<NewsSummaryDTO> getRecentNewsMixed(int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            
            // Obtener más noticias para hacer la mezcla
            List<News> allRecentNews = newsRepository.findRecentNews(since);
            
            if (allRecentNews.isEmpty()) {
                log.info("No hay noticias recientes en las últimas 24h, obteniendo las más recientes");
                // Si no hay noticias en 24h, obtener las más recientes en general
                Pageable pageable = PageRequest.of(0, limit * 2, 
                    Sort.by(Sort.Direction.DESC, "publishedDate"));
                Page<News> recentPage = newsRepository.findByIsActiveTrueOrderByPublishedDateDesc(pageable);
                allRecentNews = recentPage.getContent();
            }
            
            List<News> mixedNews = diversifyNewsBySource(allRecentNews, limit);
            
            return mixedNews.stream()
                    .map(this::convertToSummaryDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error obteniendo noticias recientes: {}", e.getMessage());
            throw new RuntimeException("Error al obtener noticias recientes", e);
        }
    }
    
    /**
     * NUEVO: Diversificar noticias por fuentes para evitar monopolio de una sola fuente
     */
    private List<News> diversifyNewsBySource(List<News> allNews, int targetSize) {
        if (allNews.isEmpty()) {
            return allNews;
        }
        
        // Agrupar por fuente
        Map<String, List<News>> newsBySource = allNews.stream()
                .collect(Collectors.groupingBy(News::getSource));
        
        log.debug("Fuentes encontradas: {}", newsBySource.keySet());
        
        List<News> diversifiedNews = new ArrayList<>();
        
        // Algoritmo de distribución balanceada
        int maxPerSource = Math.max(1, targetSize / newsBySource.size());
        int remaining = targetSize;
        
        // Primera pasada: tomar hasta maxPerSource de cada fuente
        for (Map.Entry<String, List<News>> entry : newsBySource.entrySet()) {
            List<News> sourceNews = entry.getValue();
            int toTake = Math.min(maxPerSource, Math.min(sourceNews.size(), remaining));
            
            diversifiedNews.addAll(sourceNews.subList(0, toTake));
            remaining -= toTake;
            
            if (remaining <= 0) break;
        }
        
        // Segunda pasada: completar con noticias restantes si aún falta
        if (remaining > 0) {
            for (News news : allNews) {
                if (!diversifiedNews.contains(news) && remaining > 0) {
                    diversifiedNews.add(news);
                    remaining--;
                }
            }
        }
        
        // Ordenar por fecha/popularidad original
        diversifiedNews.sort((n1, n2) -> {
            // Primero por viewCount desc, luego por fecha desc
            int viewCompare = Integer.compare(n2.getViewCount(), n1.getViewCount());
            if (viewCompare != 0) return viewCompare;
            
            if (n1.getPublishedDate() == null && n2.getPublishedDate() == null) return 0;
            if (n1.getPublishedDate() == null) return 1;
            if (n2.getPublishedDate() == null) return -1;
            
            return n2.getPublishedDate().compareTo(n1.getPublishedDate());
        });
        
        log.debug("Diversificación completada: {} noticias de {} fuentes", 
                diversifiedNews.size(), newsBySource.size());
        
        return diversifiedNews.subList(0, Math.min(targetSize, diversifiedNews.size()));
    }
    
    /**
     * Obtener estadísticas generales - MEJORADO CON VALIDACIONES
     */
    public NewsStatsDTO getNewsStats() {
        try {
            long totalNews = newsRepository.count();
            
            LocalDateTime todayStart = LocalDateTime.now()
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            
            List<News> todayNews = newsRepository.findRecentNews(todayStart);
            List<News> weekNews = newsRepository.findRecentNews(weekStart);
            
            List<Object[]> sourceStatsRaw = newsRepository.countNewsBySource();
            List<SourceStatsDTO> sourceStats = sourceStatsRaw.stream()
                    .map(row -> SourceStatsDTO.builder()
                            .source((String) row[0])
                            .count((Long) row[1])
                            .build())
                    .collect(Collectors.toList());
            
            List<String> categories = newsRepository.findDistinctCategories();
            
            return NewsStatsDTO.builder()
                    .totalNews(totalNews)
                    .todayNews(todayNews.size())
                    .thisWeekNews(weekNews.size())
                    .sourceStats(sourceStats)
                    .categories(categories != null ? categories : new ArrayList<>())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener estadísticas", e);
        }
    }
    
    /**
     * Guardar nueva noticia (usado por el scraper) - MEJORADO
     */
    @Transactional
    public News saveNews(News news) {
        try {
            // Verificar si ya existe por URL
            Optional<News> existing = newsRepository.findByOriginalUrl(news.getOriginalUrl());
            
            if (existing.isPresent()) {
                log.debug("Noticia ya existe: {}", news.getOriginalUrl());
                return existing.get();
            }
            
            // Validaciones adicionales
            if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("El título no puede estar vacío");
            }
            
            if (news.getOriginalUrl() == null || news.getOriginalUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("La URL original no puede estar vacía");
            }
            
            log.info("Guardando nueva noticia: {}", news.getTitle());
            return newsRepository.save(news);
            
        } catch (Exception e) {
            log.error("Error guardando noticia: {}", e.getMessage());
            throw new RuntimeException("Error al guardar noticia", e);
        }
    }
    
    // Métodos de conversión privados - MEJORADOS CON VALIDACIONES
    private NewsDTO convertToDTO(News news) {
        if (news == null) return null;
        
        return NewsDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .summary(news.getSummary())
                .originalUrl(news.getOriginalUrl())
                .imageUrl(news.getImageUrl())
                .source(news.getSource())
                .author(news.getAuthor())
                .category(news.getCategory())
                .publishedDate(news.getPublishedDate())
                .createdAt(news.getCreatedAt())
                .viewCount(news.getViewCount() != null ? news.getViewCount() : 0)
                .build();
    }
    
    private NewsSummaryDTO convertToSummaryDTO(News news) {
        if (news == null) return null;
        
        return NewsSummaryDTO.builder()
                .id(news.getId())
                .title(news.getTitle())
                .summary(news.getSummary())
                .imageUrl(news.getImageUrl())
                .source(news.getSource())
                .category(news.getCategory())
                .publishedDate(news.getPublishedDate())
                .viewCount(news.getViewCount() != null ? news.getViewCount() : 0)
                .build();
    }
    
    private PagedNewsResponse convertToPagedResponse(Page<News> newsPage) {
        if (newsPage == null) {
            return createEmptyResponse();
        }
        
        return PagedNewsResponse.builder()
                .news(newsPage.getContent().stream()
                        .map(this::convertToSummaryDTO)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .currentPage(newsPage.getNumber())
                .totalPages(newsPage.getTotalPages())
                .totalElements(newsPage.getTotalElements())
                .hasNext(newsPage.hasNext())
                .hasPrevious(newsPage.hasPrevious())
                .build();
    }
    
    private Pageable createPageable(NewsSearchRequest request) {
        try {
            Sort.Direction direction = Sort.Direction.fromString(request.getSortDirection());
            Sort sort = Sort.by(direction, request.getSortBy());
            return PageRequest.of(request.getPage(), request.getSize(), sort);
        } catch (Exception e) {
            log.warn("Error creando pageable, usando valores por defecto: {}", e.getMessage());
            return PageRequest.of(request.getPage(), request.getSize(), 
                    Sort.by(Sort.Direction.DESC, "publishedDate"));
        }
    }
    
    private PagedNewsResponse createEmptyResponse() {
        return PagedNewsResponse.builder()
                .news(new ArrayList<>())
                .currentPage(0)
                .totalPages(0)
                .totalElements(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}