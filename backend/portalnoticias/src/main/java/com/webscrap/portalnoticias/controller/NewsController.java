package com.webscrap.portalnoticias.controller;

import com.webscrap.portalnoticias.dto.*;
import com.webscrap.portalnoticias.entity.NewsSource;
import com.webscrap.portalnoticias.service.NewsService;
import com.webscrap.portalnoticias.service.NewsSourceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins}")
public class NewsController {
    
    private final NewsService newsService;
    private final NewsSourceService newsSourceService;
    
    /**
     * Obtener todas las noticias con paginación
     */
    @GetMapping
    public ResponseEntity<PagedNewsResponse> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Obteniendo noticias - Página: {}, Tamaño: {}, Orden: {} {}", 
                page, size, sortBy, sortDirection);
        
        try {
            PagedNewsResponse response = newsService.getAllNews(page, size, sortBy, sortDirection);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo todas las noticias: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener noticia por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        log.info("Obteniendo noticia con ID: {}", id);
        
        try {
            Optional<NewsDTO> news = newsService.getNewsById(id);
            return news.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error obteniendo noticia por ID {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Buscar noticias - CORREGIDO
     */
    @PostMapping("/search")
    public ResponseEntity<PagedNewsResponse> searchNews(@RequestBody NewsSearchRequest request) {
        log.info("Búsqueda de noticias: {}", request);
        
        try {
            // Validar request
            if (request == null) {
                return ResponseEntity.badRequest().build();
            }
            
            PagedNewsResponse response = newsService.searchNews(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en búsqueda de noticias: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda simple por palabra clave (GET) - CORREGIDO
     */
    @GetMapping("/search")
    public ResponseEntity<PagedNewsResponse> searchNewsByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Búsqueda simple por palabra clave: {}", keyword);
        
        try {
            // Validar keyword
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            NewsSearchRequest request = NewsSearchRequest.builder()
                    .keyword(keyword.trim())
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            PagedNewsResponse response = newsService.searchNews(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error en búsqueda por keyword '{}': {}", keyword, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener noticias por ID de fuente - MEJORADO CON ID EN LUGAR DE NOMBRE
     */
    @GetMapping("/source/{sourceId}")
    public ResponseEntity<PagedNewsResponse> getNewsBySourceId(
            @PathVariable Long sourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Obteniendo noticias de fuente ID: {}", sourceId);
        
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            if (source == null) {
                log.warn("Fuente no encontrada con ID: {}", sourceId);
                return ResponseEntity.notFound().build();
            }
            
            NewsSearchRequest request = NewsSearchRequest.builder()
                    .source(source.getName())
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            PagedNewsResponse response = newsService.searchNews(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo noticias por fuente ID {}: {}", sourceId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * DEPRECATED: Mantener para compatibilidad, pero recomendar usar por ID
     */
    @GetMapping("/source/name/{sourceName}")
    public ResponseEntity<PagedNewsResponse> getNewsBySourceName(
            @PathVariable String sourceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Obteniendo noticias de fuente: {} (DEPRECATED - usar /source/{id})", sourceName);
        
        try {
            NewsSearchRequest request = NewsSearchRequest.builder()
                    .source(sourceName)
                    .page(page)
                    .size(size)
                    .build();
            
            PagedNewsResponse response = newsService.searchNews(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo noticias por nombre fuente '{}': {}", sourceName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener noticias por categoría - CORREGIDO
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<PagedNewsResponse> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("Obteniendo noticias de categoría: {}", category);
        
        try {
            NewsSearchRequest request = NewsSearchRequest.builder()
                    .category(category)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            PagedNewsResponse response = newsService.searchNews(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo noticias por categoría '{}': {}", category, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener noticias más populares - MEJORADO CON MEZCLA DE FUENTES
     */
    @GetMapping("/popular")
    public ResponseEntity<PagedNewsResponse> getMostPopularNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Obteniendo noticias más populares");
        
        try {
            PagedNewsResponse response = newsService.getMostPopularNewsMixed(page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obteniendo noticias populares: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener noticias recientes (últimas 24 horas) - MEJORADO CON MEZCLA DE FUENTES
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NewsSummaryDTO>> getRecentNews(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Obteniendo noticias recientes (últimas 24h)");
        
        try {
            List<NewsSummaryDTO> recentNews = newsService.getRecentNewsMixed(limit);
            return ResponseEntity.ok(recentNews);
        } catch (Exception e) {
            log.error("Error obteniendo noticias recientes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener estadísticas generales
     */
    @GetMapping("/stats")
    public ResponseEntity<NewsStatsDTO> getNewsStats() {
        log.info("Obteniendo estadísticas de noticias");
        
        try {
            NewsStatsDTO stats = newsService.getNewsStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todas las fuentes disponibles
     */
    @GetMapping("/sources")
    public ResponseEntity<List<Map<String, Object>>> getAllSources() {
        log.info("Obteniendo todas las fuentes");
        
        try {
            List<NewsSource> sources = newsSourceService.getAllSources();
            List<Map<String, Object>> sourcesList = sources.stream()
                    .map(this::convertSourceToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(sourcesList);
        } catch (Exception e) {
            log.error("Error obteniendo fuentes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener solo fuentes activas
     */
    @GetMapping("/sources/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveSources() {
        log.info("Obteniendo fuentes activas");
        
        try {
            List<NewsSource> sources = newsSourceService.getActiveSources();
            List<Map<String, Object>> sourcesList = sources.stream()
                    .map(this::convertSourceToMap)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(sourcesList);
        } catch (Exception e) {
            log.error("Error obteniendo fuentes activas: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todas las categorías disponibles
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("Obteniendo todas las categorías");
        
        try {
            NewsStatsDTO stats = newsService.getNewsStats();
            return ResponseEntity.ok(stats.getCategories());
        } catch (Exception e) {
            log.error("Error obteniendo categorías: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Helper para convertir NewsSource a Map
     */
    private Map<String, Object> convertSourceToMap(NewsSource source) {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("id", source.getId());
        sourceMap.put("name", source.getName());
        sourceMap.put("isActive", source.getIsActive());
        sourceMap.put("lastScrapedAt", source.getLastScrapedAt());
        sourceMap.put("successfulScrapes", source.getSuccessfulScrapes());
        sourceMap.put("failedScrapes", source.getFailedScrapes());
        sourceMap.put("baseUrl", source.getBaseUrl());
        return sourceMap;
    }
}