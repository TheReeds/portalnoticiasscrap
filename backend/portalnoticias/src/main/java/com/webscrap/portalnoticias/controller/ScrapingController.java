package com.webscrap.portalnoticias.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webscrap.portalnoticias.dto.NewsSourceCreateDTO;
import com.webscrap.portalnoticias.dto.NewsSourceUpdateDTO;
import com.webscrap.portalnoticias.entity.News;
import com.webscrap.portalnoticias.entity.NewsSource;
import com.webscrap.portalnoticias.scheduler.NewsScheduler;
import com.webscrap.portalnoticias.service.NewsSourceService;
import com.webscrap.portalnoticias.service.ScrapingService;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scraping")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins}")
public class ScrapingController {
    
    private final ScrapingService scrapingService;
    private final NewsSourceService newsSourceService;
    private final NewsScheduler newsScheduler;
    
    /**
     * Ejecutar scraping manual de todas las fuentes activas
     */
    @PostMapping("/run-all")
    public ResponseEntity<Map<String, Object>> runScrapingForAllSources() {
        log.info("Iniciando scraping manual de todas las fuentes");
        
        try {
            List<NewsSource> activeSources = newsSourceService.getActiveSources();
            
            if (activeSources.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No hay fuentes activas configuradas"));
            }
            
            int totalNews = 0;
            int successfulSources = 0;
            Map<String, Object> sourceResults = new HashMap<>();
            
            for (NewsSource source : activeSources) {
                try {
                    List<News> scrapedNews = scrapingService.scrapeNewsSource(source);
                    totalNews += scrapedNews.size();
                    
                    Map<String, Object> sourceResult = new HashMap<>();
                    sourceResult.put("id", source.getId());
                    sourceResult.put("newsCount", scrapedNews.size());
                    sourceResult.put("status", "success");
                    sourceResults.put(source.getName(), sourceResult);
                    
                    newsSourceService.updateSourceStats(source.getId(), true, scrapedNews.size());
                    successfulSources++;
                    
                } catch (Exception e) {
                    log.error("Error en scraping manual de {}: {}", source.getName(), e.getMessage());
                    
                    Map<String, Object> sourceResult = new HashMap<>();
                    sourceResult.put("id", source.getId());
                    sourceResult.put("newsCount", 0);
                    sourceResult.put("status", "error");
                    sourceResult.put("error", e.getMessage());
                    sourceResults.put(source.getName(), sourceResult);
                    
                    newsSourceService.updateSourceStats(source.getId(), false, 0);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Scraping manual completado");
            response.put("totalNews", totalNews);
            response.put("successfulSources", successfulSources);
            response.put("totalSources", activeSources.size());
            response.put("results", sourceResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error general en scraping manual: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }
    
    /**
     * Ejecutar scraping manual de una fuente específica POR ID
     */
    @PostMapping("/run/{sourceId}")
    public ResponseEntity<Map<String, Object>> runScrapingForSource(@PathVariable Long sourceId) {
        log.info("Iniciando scraping manual para fuente ID: {}", sourceId);
        
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            
            if (source == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!source.getIsActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La fuente está inactiva", "sourceId", sourceId, "sourceName", source.getName()));
            }
            
            List<News> scrapedNews = scrapingService.scrapeNewsSource(source);
            newsSourceService.updateSourceStats(source.getId(), true, scrapedNews.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Scraping completado exitosamente");
            response.put("sourceId", source.getId());
            response.put("sourceName", source.getName());
            response.put("newsScraped", scrapedNews.size());
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en scraping manual de fuente {}: {}", sourceId, e.getMessage());
            newsSourceService.updateSourceStats(sourceId, false, 0);
            
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error en scraping: " + e.getMessage(), "sourceId", sourceId));
        }
    }
    
    /**
     * NUEVA: Crear una nueva fuente de noticias
     */
    @PostMapping("/sources")
    public ResponseEntity<Map<String, Object>> createNewsSource(@Valid @RequestBody NewsSourceCreateDTO createDTO) {
        log.info("Creando nueva fuente de noticias: {}", createDTO.getName());
        
        try {
            // Validar que no exista una fuente con el mismo nombre
            if (newsSourceService.existsByName(createDTO.getName())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ya existe una fuente con el nombre: " + createDTO.getName()));
            }
            
            // Validar que no exista una fuente con la misma URL base
            if (newsSourceService.existsByBaseUrl(createDTO.getBaseUrl())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Ya existe una fuente con la URL: " + createDTO.getBaseUrl()));
            }
            
            NewsSource newSource = NewsSource.builder()
                    .name(createDTO.getName().trim())
                    .baseUrl(createDTO.getBaseUrl().trim())
                    .newsListSelector(createDTO.getNewsListSelector())
                    .titleSelector(createDTO.getTitleSelector())
                    .contentSelector(createDTO.getContentSelector())
                    .summarySelector(createDTO.getSummarySelector())
                    .imageSelector(createDTO.getImageSelector())
                    .authorSelector(createDTO.getAuthorSelector())
                    .dateSelector(createDTO.getDateSelector())
                    .categorySelector(createDTO.getCategorySelector())
                    .dateFormat(createDTO.getDateFormat())
                    .isActive(createDTO.getIsActive() != null ? createDTO.getIsActive() : true)
                    .scrapingIntervalMinutes(createDTO.getScrapingIntervalMinutes() != null ? 
                            createDTO.getScrapingIntervalMinutes() : 30)
                    .build();
            
            NewsSource savedSource = newsSourceService.saveSource(newSource);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fuente creada exitosamente");
            response.put("source", convertSourceToResponse(savedSource));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error creando fuente: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error creando fuente: " + e.getMessage()));
        }
    }
    
    /**
     * NUEVA: Actualizar una fuente existente
     */
    @PutMapping("/sources/{sourceId}")
    public ResponseEntity<Map<String, Object>> updateNewsSource(
            @PathVariable Long sourceId, 
            @Valid @RequestBody NewsSourceUpdateDTO updateDTO) {
        
        log.info("Actualizando fuente ID: {}", sourceId);
        
        try {
            NewsSource existingSource = newsSourceService.getSourceById(sourceId);
            
            if (existingSource == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Actualizar solo los campos que vienen en el DTO
            if (updateDTO.getName() != null) {
                existingSource.setName(updateDTO.getName().trim());
            }
            if (updateDTO.getBaseUrl() != null) {
                existingSource.setBaseUrl(updateDTO.getBaseUrl().trim());
            }
            if (updateDTO.getNewsListSelector() != null) {
                existingSource.setNewsListSelector(updateDTO.getNewsListSelector());
            }
            if (updateDTO.getTitleSelector() != null) {
                existingSource.setTitleSelector(updateDTO.getTitleSelector());
            }
            if (updateDTO.getContentSelector() != null) {
                existingSource.setContentSelector(updateDTO.getContentSelector());
            }
            if (updateDTO.getSummarySelector() != null) {
                existingSource.setSummarySelector(updateDTO.getSummarySelector());
            }
            if (updateDTO.getImageSelector() != null) {
                existingSource.setImageSelector(updateDTO.getImageSelector());
            }
            if (updateDTO.getAuthorSelector() != null) {
                existingSource.setAuthorSelector(updateDTO.getAuthorSelector());
            }
            if (updateDTO.getDateSelector() != null) {
                existingSource.setDateSelector(updateDTO.getDateSelector());
            }
            if (updateDTO.getCategorySelector() != null) {
                existingSource.setCategorySelector(updateDTO.getCategorySelector());
            }
            if (updateDTO.getDateFormat() != null) {
                existingSource.setDateFormat(updateDTO.getDateFormat());
            }
            if (updateDTO.getIsActive() != null) {
                existingSource.setIsActive(updateDTO.getIsActive());
            }
            if (updateDTO.getScrapingIntervalMinutes() != null) {
                existingSource.setScrapingIntervalMinutes(updateDTO.getScrapingIntervalMinutes());
            }
            
            NewsSource updatedSource = newsSourceService.saveSource(existingSource);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fuente actualizada exitosamente");
            response.put("source", convertSourceToResponse(updatedSource));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error actualizando fuente {}: {}", sourceId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error actualizando fuente: " + e.getMessage()));
        }
    }
    
    /**
     * NUEVA: Eliminar una fuente (soft delete)
     */
    @DeleteMapping("/sources/{sourceId}")
    public ResponseEntity<Map<String, Object>> deleteNewsSource(@PathVariable Long sourceId) {
        log.info("Eliminando fuente ID: {}", sourceId);
        
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            
            if (source == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Soft delete: marcar como inactiva
            source.setIsActive(false);
            newsSourceService.saveSource(source);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fuente desactivada exitosamente");
            response.put("sourceId", sourceId);
            response.put("sourceName", source.getName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error eliminando fuente {}: {}", sourceId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error eliminando fuente: " + e.getMessage()));
        }
    }
    
    /**
     * NUEVA: Obtener detalles completos de una fuente
     */
    @GetMapping("/sources/{sourceId}")
    public ResponseEntity<Map<String, Object>> getSourceDetails(@PathVariable Long sourceId) {
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            
            if (source == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(convertSourceToResponse(source));
            
        } catch (Exception e) {
            log.error("Error obteniendo detalles de fuente {}: {}", sourceId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error obteniendo fuente: " + e.getMessage()));
        }
    }
    
    /**
     * Obtener estado de las fuentes de scraping - MEJORADO
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getScrapingStatus() {
        try {
            List<NewsSource> allSources = newsSourceService.getAllSources();
            List<NewsSource> activeSources = newsSourceService.getActiveSources();
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalSources", allSources.size());
            response.put("activeSources", activeSources.size());
            response.put("inactiveSources", allSources.size() - activeSources.size());
            response.put("sources", allSources.stream().map(this::convertSourceToResponse).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error obteniendo estado de scraping: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error obteniendo estado: " + e.getMessage()));
        }
    }
    
    /**
     * Activar/desactivar una fuente de scraping - MEJORADO USANDO ID
     */
    @PutMapping("/sources/{sourceId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSource(@PathVariable Long sourceId) {
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            
            if (source == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean previousState = source.getIsActive();
            source.setIsActive(!previousState);
            newsSourceService.saveSource(source);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Estado de fuente actualizado exitosamente");
            response.put("sourceId", source.getId());
            response.put("sourceName", source.getName());
            response.put("previousState", previousState);
            response.put("currentState", source.getIsActive());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cambiando estado de fuente {}: {}", sourceId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error cambiando estado: " + e.getMessage()));
        }
    }
    
    /**
     * NUEVA: Probar una fuente antes de guardarla
     */
    @PostMapping("/sources/test")
    public ResponseEntity<Map<String, Object>> testNewsSource(@RequestBody NewsSourceCreateDTO testDTO) {
        log.info("Probando configuración de fuente: {}", testDTO.getName());
        
        try {
            // Crear fuente temporal (sin guardar en BD)
            NewsSource tempSource = NewsSource.builder()
                    .name(testDTO.getName())
                    .baseUrl(testDTO.getBaseUrl())
                    .newsListSelector(testDTO.getNewsListSelector())
                    .titleSelector(testDTO.getTitleSelector())
                    .contentSelector(testDTO.getContentSelector())
                    .summarySelector(testDTO.getSummarySelector())
                    .imageSelector(testDTO.getImageSelector())
                    .authorSelector(testDTO.getAuthorSelector())
                    .dateSelector(testDTO.getDateSelector())
                    .categorySelector(testDTO.getCategorySelector())
                    .dateFormat(testDTO.getDateFormat())
                    .build();
            
            // Intentar scraping de prueba (limitado a 5 noticias)
            List<News> testResults = scrapingService.scrapeNewsSource(tempSource);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Prueba completada exitosamente");
            response.put("sourceName", testDTO.getName());
            response.put("newsFound", testResults.size());
            response.put("status", "success");
            
            // Mostrar ejemplos de las primeras 3 noticias encontradas
            List<Map<String, Object>> samples = new ArrayList<>();
            for (int i = 0; i < Math.min(3, testResults.size()); i++) {
                News news = testResults.get(i);
                Map<String, Object> sample = new HashMap<>();
                sample.put("title", news.getTitle());
                sample.put("summary", news.getSummary() != null ? news.getSummary().substring(0, Math.min(100, news.getSummary().length())) + "..." : null);
                sample.put("imageUrl", news.getImageUrl());
                sample.put("author", news.getAuthor());
                sample.put("category", news.getCategory());
                sample.put("publishedDate", news.getPublishedDate());
                samples.add(sample);
            }
            response.put("samples", samples);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error probando fuente: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error en la prueba de la fuente");
            response.put("error", e.getMessage());
            response.put("status", "error");
            response.put("suggestions", List.of(
                "Verificar que la URL sea accesible",
                "Revisar los selectores CSS",
                "Comprobar si la página requiere JavaScript",
                "Validar el formato de fecha si se especificó"
            ));
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Debugging: Ver HTML de una página para ajustar selectores - MEJORADO
     */
    @GetMapping("/sources/{sourceId}/debug")
    public ResponseEntity<Map<String, Object>> debugSource(@PathVariable Long sourceId) {
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            
            if (source == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Hacer request básico para ver la estructura
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(source.getBaseUrl())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("sourceId", source.getId());
            response.put("sourceName", source.getName());
            response.put("url", source.getBaseUrl());
            response.put("pageTitle", doc.title());
            response.put("htmlLength", doc.html().length());
            
            // Probar el selector configurado
            org.jsoup.select.Elements configuredElements = doc.select(source.getNewsListSelector());
            response.put("configuredSelector", source.getNewsListSelector());
            response.put("configuredSelectorResults", configuredElements.size());
            
            // Probar selectores alternativos
            Map<String, Integer> alternativeResults = new HashMap<>();
            String[] alternatives = {
                "article", 
                "div[class*='story']", 
                "div[class*='article']",
                "div[class*='news']",
                "h2", 
                "h3",
                "a[href*='news']",
                ".entry",
                ".post",
                "[class*='item']"
            };
            
            for (String selector : alternatives) {
                try {
                    int count = doc.select(selector).size();
                    alternativeResults.put(selector, count);
                } catch (Exception e) {
                    alternativeResults.put(selector, -1);
                }
            }
            response.put("alternativeSelectors", alternativeResults);
            
            // Mostrar algunos elementos encontrados con el selector principal
            if (configuredElements.size() > 0) {
                List<String> sampleElements = new ArrayList<>();
                for (int i = 0; i < Math.min(3, configuredElements.size()); i++) {
                    org.jsoup.nodes.Element element = configuredElements.get(i);
                    String elementHtml = element.outerHtml();
                    sampleElements.add(elementHtml.substring(0, Math.min(300, elementHtml.length())) + "...");
                }
                response.put("sampleElements", sampleElements);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error debugging fuente {}: {}", sourceId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error debugging: " + e.getMessage(), "sourceId", sourceId));
        }
    }
    
    /**
     * Helper para convertir NewsSource a respuesta API
     */
    private Map<String, Object> convertSourceToResponse(NewsSource source) {
        Map<String, Object> sourceMap = new HashMap<>();
        sourceMap.put("id", source.getId());
        sourceMap.put("name", source.getName());
        sourceMap.put("baseUrl", source.getBaseUrl());
        sourceMap.put("newsListSelector", source.getNewsListSelector());
        sourceMap.put("titleSelector", source.getTitleSelector());
        sourceMap.put("contentSelector", source.getContentSelector());
        sourceMap.put("summarySelector", source.getSummarySelector());
        sourceMap.put("imageSelector", source.getImageSelector());
        sourceMap.put("authorSelector", source.getAuthorSelector());
        sourceMap.put("dateSelector", source.getDateSelector());
        sourceMap.put("categorySelector", source.getCategorySelector());
        sourceMap.put("dateFormat", source.getDateFormat());
        sourceMap.put("isActive", source.getIsActive());
        sourceMap.put("scrapingIntervalMinutes", source.getScrapingIntervalMinutes());
        sourceMap.put("lastScrapedAt", source.getLastScrapedAt());
        sourceMap.put("successfulScrapes", source.getSuccessfulScrapes());
        sourceMap.put("failedScrapes", source.getFailedScrapes());
        sourceMap.put("createdAt", source.getCreatedAt());
        sourceMap.put("updatedAt", source.getUpdatedAt());
        return sourceMap;
    }
}