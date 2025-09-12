package com.webscrap.portalnoticias.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webscrap.portalnoticias.entity.News;
import com.webscrap.portalnoticias.entity.NewsSource;
import com.webscrap.portalnoticias.service.NewsSourceService;
import com.webscrap.portalnoticias.service.ScrapingService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "scraping.enabled", havingValue = "true", matchIfMissing = true)
public class NewsScheduler {
    
    private final ScrapingService scrapingService;
    private final NewsSourceService newsSourceService;
    
    @Value("${scraping.interval.minutes:30}")
    private int scrapingIntervalMinutes;
    
    /**
     * Ejecutar scraping automático cada X minutos (configurable)
     * Por defecto cada 30 minutos
     */
    @Scheduled(fixedDelayString = "${scraping.interval.minutes:30}000", initialDelay = 10000)
    @Async
    public void performScheduledScraping() {
        log.info("=== Iniciando scraping programado ===");
        
        try {
            List<NewsSource> activeSources = newsSourceService.getActiveSources();
            
            if (activeSources.isEmpty()) {
                log.warn("No hay fuentes activas configuradas para scraping");
                return;
            }
            
            log.info("Fuentes activas encontradas: {}", activeSources.size());
            
            int totalNewsScraped = 0;
            int successfulSources = 0;
            
            for (NewsSource source : activeSources) {
                try {
                    // Verificar si es tiempo de hacer scraping de esta fuente
                    if (shouldScrapeSource(source)) {
                        log.info("Procesando fuente: {}", source.getName());
                        
                        List<News> scrapedNews = scrapingService.scrapeNewsSource(source);
                        totalNewsScraped += scrapedNews.size();
                        
                        // Actualizar estadísticas de la fuente
                        newsSourceService.updateSourceStats(source.getId(), true, scrapedNews.size());
                        successfulSources++;
                        
                        log.info("Completado scraping de {}: {} noticias", 
                                source.getName(), scrapedNews.size());
                        
                        // Pequeña pausa entre fuentes para no sobrecargar
                        Thread.sleep(5000);
                        
                    } else {
                        log.debug("Saltando fuente {} - aún no es tiempo de scraping", source.getName());
                    }
                    
                } catch (Exception e) {
                    log.error("Error en scraping de fuente {}: {}", source.getName(), e.getMessage());
                    
                    // Actualizar estadísticas de error
                    newsSourceService.updateSourceStats(source.getId(), false, 0);
                }
            }
            
            log.info("=== Scraping programado completado ===");
            log.info("Total noticias extraídas: {}", totalNewsScraped);
            log.info("Fuentes exitosas: {}/{}", successfulSources, activeSources.size());
            
        } catch (Exception e) {
            log.error("Error general en scraping programado: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verificar si es tiempo de hacer scraping de una fuente específica
     */
    private boolean shouldScrapeSource(NewsSource source) {
        if (source.getLastScrapedAt() == null) {
            return true; // Primera vez
        }
        
        LocalDateTime nextScrapingTime = source.getLastScrapedAt()
                .plusMinutes(source.getScrapingIntervalMinutes());
        
        return LocalDateTime.now().isAfter(nextScrapingTime);
    }
    
    /**
     * Scraping manual para una fuente específica
     */
    @Async
    public void scrapeSourceById(Long sourceId) {
        log.info("Iniciando scraping manual para fuente ID: {}", sourceId);
        
        try {
            NewsSource source = newsSourceService.getSourceById(sourceId);
            if (source == null) {
                log.error("Fuente no encontrada con ID: {}", sourceId);
                return;
            }
            
            if (!source.getIsActive()) {
                log.warn("Fuente {} está inactiva", source.getName());
                return;
            }
            
            List<News> scrapedNews = scrapingService.scrapeNewsSource(source);
            newsSourceService.updateSourceStats(source.getId(), true, scrapedNews.size());
            
            log.info("Scraping manual completado para {}: {} noticias", 
                    source.getName(), scrapedNews.size());
            
        } catch (Exception e) {
            log.error("Error en scraping manual de fuente {}: {}", sourceId, e.getMessage());
            newsSourceService.updateSourceStats(sourceId, false, 0);
        }
    }
    
    /**
     * Limpieza de noticias antiguas - ejecutar diariamente a las 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Async
    public void cleanupOldNews() {
        log.info("=== Iniciando limpieza de noticias antiguas ===");
        
        try {
            // Eliminar noticias más antiguas de 6 meses
            LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(6);
            int deletedCount = newsSourceService.deleteOldNews(cutoffDate);
            
            log.info("Limpieza completada: {} noticias eliminadas", deletedCount);
            
        } catch (Exception e) {
            log.error("Error en limpieza de noticias antiguas: {}", e.getMessage());
        }
    }
}