package com.webscrap.portalnoticias.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webscrap.portalnoticias.entity.News;
import com.webscrap.portalnoticias.entity.NewsSource;
import com.webscrap.portalnoticias.repository.NewsRepository;
import com.webscrap.portalnoticias.repository.NewsSourceRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NewsSourceService {
    
    private final NewsSourceRepository newsSourceRepository;
    private final NewsRepository newsRepository;
    
    /**
     * Obtener todas las fuentes activas
     */
    public List<NewsSource> getActiveSources() {
        try {
            return newsSourceRepository.findByIsActiveTrue();
        } catch (Exception e) {
            log.error("Error obteniendo fuentes activas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener fuentes activas", e);
        }
    }
    
    /**
     * Obtener fuente por ID - VALIDADO
     */
    public NewsSource getSourceById(Long id) {
        if (id == null) {
            log.warn("ID de fuente es null");
            return null;
        }
        
        try {
            return newsSourceRepository.findById(id).orElse(null);
        } catch (Exception e) {
            log.error("Error obteniendo fuente por ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener fuente por ID", e);
        }
    }
    
    /**
     * NUEVA: Verificar si existe fuente por nombre
     */
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        try {
            return newsSourceRepository.findByName(name.trim()).isPresent();
        } catch (Exception e) {
            log.error("Error verificando existencia por nombre '{}': {}", name, e.getMessage());
            throw new RuntimeException("Error al verificar existencia por nombre", e);
        }
    }
    
    /**
     * NUEVA: Verificar si existe fuente por URL base
     */
    public boolean existsByBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return false;
        }
        
        try {
            return newsSourceRepository.findByBaseUrl(baseUrl.trim()).isPresent();
        } catch (Exception e) {
            log.error("Error verificando existencia por URL '{}': {}", baseUrl, e.getMessage());
            throw new RuntimeException("Error al verificar existencia por URL", e);
        }
    }
    
    /**
     * NUEVA: Obtener fuente por nombre
     */
    public Optional<NewsSource> getSourceByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        try {
            return newsSourceRepository.findByName(name.trim());
        } catch (Exception e) {
            log.error("Error obteniendo fuente por nombre '{}': {}", name, e.getMessage());
            throw new RuntimeException("Error al obtener fuente por nombre", e);
        }
    }
    
    /**
     * NUEVA: Buscar fuentes por patrón de nombre
     */
    public List<NewsSource> searchSourcesByName(String namePattern) {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            return getAllSources();
        }
        
        try {
            return newsSourceRepository.findByNameContainingIgnoreCase(namePattern.trim());
        } catch (Exception e) {
            log.error("Error buscando fuentes por patrón '{}': {}", namePattern, e.getMessage());
            throw new RuntimeException("Error al buscar fuentes por patrón", e);
        }
    }
    
    /**
     * Actualizar estadísticas de scraping de una fuente
     */
    @Transactional
    public void updateSourceStats(Long sourceId, boolean successful, int newsCount) {
        if (sourceId == null) {
            log.error("ID de fuente es null para actualizar stats");
            return;
        }
        
        try {
            NewsSource source = newsSourceRepository.findById(sourceId).orElse(null);
            if (source == null) {
                log.error("Fuente no encontrada para actualizar stats: {}", sourceId);
                return;
            }
            
            if (successful) {
                source.incrementSuccessfulScrapes();
                log.info("Scraping exitoso para {} (ID: {}): {} noticias nuevas", 
                        source.getName(), sourceId, newsCount);
            } else {
                source.incrementFailedScrapes();
                log.warn("Scraping fallido para {} (ID: {})", source.getName(), sourceId);
            }
            
            newsSourceRepository.save(source);
            
        } catch (Exception e) {
            log.error("Error actualizando estadísticas de fuente {}: {}", sourceId, e.getMessage());
            throw new RuntimeException("Error al actualizar estadísticas", e);
        }
    }
    
    /**
     * Eliminar noticias antiguas
     */
    @Transactional
    public int deleteOldNews(LocalDateTime cutoffDate) {
        if (cutoffDate == null) {
            log.error("Fecha de corte es null");
            return 0;
        }
        
        try {
            // En lugar de eliminar, marcar como inactivas
            List<News> oldNews = newsRepository.findByCreatedAtBeforeAndIsActiveTrue(cutoffDate);
            
            int count = 0;
            for (News news : oldNews) {
                news.setIsActive(false);
                count++;
            }
            
            if (count > 0) {
                newsRepository.saveAll(oldNews);
                log.info("Marcadas como inactivas {} noticias anteriores a {}", count, cutoffDate);
            }
            
            return count;
            
        } catch (Exception e) {
            log.error("Error eliminando noticias antiguas: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar noticias antiguas", e);
        }
    }
    
    /**
     * Crear o actualizar una fuente de noticias - VALIDADO
     */
    @Transactional
    public NewsSource saveSource(NewsSource source) {
        if (source == null) {
            throw new IllegalArgumentException("La fuente no puede ser null");
        }
        
        try {
            // Validaciones básicas
            if (source.getName() == null || source.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de la fuente no puede estar vacío");
            }
            
            if (source.getBaseUrl() == null || source.getBaseUrl().trim().isEmpty()) {
                throw new IllegalArgumentException("La URL base no puede estar vacía");
            }
            
            if (source.getNewsListSelector() == null || source.getNewsListSelector().trim().isEmpty()) {
                throw new IllegalArgumentException("El selector de lista de noticias no puede estar vacío");
            }
            
            // Limpiar datos
            source.setName(source.getName().trim());
            source.setBaseUrl(source.getBaseUrl().trim());
            
            // Valores por defecto
            if (source.getIsActive() == null) {
                source.setIsActive(true);
            }
            
            if (source.getScrapingIntervalMinutes() == null || source.getScrapingIntervalMinutes() < 1) {
                source.setScrapingIntervalMinutes(30);
            }
            
            if (source.getSuccessfulScrapes() == null) {
                source.setSuccessfulScrapes(0);
            }
            
            if (source.getFailedScrapes() == null) {
                source.setFailedScrapes(0);
            }
            
            NewsSource savedSource = newsSourceRepository.save(source);
            log.info("Fuente guardada: {} (ID: {})", savedSource.getName(), savedSource.getId());
            
            return savedSource;
            
        } catch (Exception e) {
            log.error("Error guardando fuente: {}", e.getMessage());
            throw new RuntimeException("Error al guardar fuente", e);
        }
    }
    
    /**
     * Obtener todas las fuentes - VALIDADO
     */
    public List<NewsSource> getAllSources() {
        try {
            return newsSourceRepository.findAll();
        } catch (Exception e) {
            log.error("Error obteniendo todas las fuentes: {}", e.getMessage());
            throw new RuntimeException("Error al obtener todas las fuentes", e);
        }
    }
    
    /**
     * NUEVA: Obtener estadísticas detalladas de fuentes
     */
    public List<Object[]> getDetailedSourceStatistics() {
        try {
            return newsSourceRepository.getSourceStatistics();
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas detalladas: {}", e.getMessage());
            throw new RuntimeException("Error al obtener estadísticas detalladas", e);
        }
    }
    
    /**
     * Usar intervalo fijo de 30 minutos
     */
    public List<NewsSource> getSourcesNeedingScraping() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
            return newsSourceRepository.findSourcesNeedingScraping(cutoffTime);
        } catch (Exception e) {
            log.error("Error obteniendo fuentes que necesitan scraping: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener fuentes para scraping", e);
        }
    }

    /**
     * Usar intervalo dinámico
     */
    public List<NewsSource> getSourcesNeedingScraping(int intervalMinutes) {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(intervalMinutes);
            return newsSourceRepository.findSourcesNeedingScraping(cutoffTime);
        } catch (Exception e) {
            log.error("Error obteniendo fuentes que necesitan scraping con intervalo {} min: {}", intervalMinutes, e.getMessage(), e);
            throw new RuntimeException("Error al obtener fuentes para scraping", e);
        }
    }

    
    /**
     * NUEVA: Activar/desactivar fuente por ID
     */
    @Transactional
    public boolean toggleSourceStatus(Long sourceId) {
        if (sourceId == null) {
            throw new IllegalArgumentException("ID de fuente no puede ser null");
        }
        
        try {
            NewsSource source = getSourceById(sourceId);
            if (source == null) {
                throw new IllegalArgumentException("Fuente no encontrada con ID: " + sourceId);
            }
            
            boolean newStatus = !source.getIsActive();
            source.setIsActive(newStatus);
            saveSource(source);
            
            log.info("Estado de fuente {} cambiado a: {}", source.getName(), newStatus);
            return newStatus;
            
        } catch (Exception e) {
            log.error("Error cambiando estado de fuente {}: {}", sourceId, e.getMessage());
            throw new RuntimeException("Error al cambiar estado de fuente", e);
        }
    }
    
    /**
     * NUEVA: Eliminar fuente permanentemente (hard delete)
     */
    @Transactional
    public boolean deleteSourcePermanently(Long sourceId) {
        if (sourceId == null) {
            throw new IllegalArgumentException("ID de fuente no puede ser null");
        }
        
        try {
            NewsSource source = getSourceById(sourceId);
            if (source == null) {
                return false;
            }
            
            // Primero marcar todas las noticias de esta fuente como inactivas
            List<News> sourceNews = newsRepository.findBySourceAndIsActiveTrueOrderByPublishedDateDesc(source.getName());
            sourceNews.forEach(news -> news.setIsActive(false));
            if (!sourceNews.isEmpty()) {
                newsRepository.saveAll(sourceNews);
            }
            
            // Luego eliminar la fuente
            newsSourceRepository.delete(source);
            
            log.warn("Fuente eliminada permanentemente: {} (ID: {})", source.getName(), sourceId);
            return true;
            
        } catch (Exception e) {
            log.error("Error eliminando fuente permanentemente {}: {}", sourceId, e.getMessage());
            throw new RuntimeException("Error al eliminar fuente permanentemente", e);
        }
    }
    
    /**
     * NUEVA: Resetear estadísticas de una fuente
     */
    @Transactional
    public void resetSourceStatistics(Long sourceId) {
        if (sourceId == null) {
            throw new IllegalArgumentException("ID de fuente no puede ser null");
        }
        
        try {
            NewsSource source = getSourceById(sourceId);
            if (source == null) {
                throw new IllegalArgumentException("Fuente no encontrada con ID: " + sourceId);
            }
            
            source.setSuccessfulScrapes(0);
            source.setFailedScrapes(0);
            source.setLastScrapedAt(null);
            saveSource(source);
            
            log.info("Estadísticas reseteadas para fuente: {} (ID: {})", source.getName(), sourceId);
            
        } catch (Exception e) {
            log.error("Error reseteando estadísticas de fuente {}: {}", sourceId, e.getMessage());
            throw new RuntimeException("Error al resetear estadísticas", e);
        }
    }
}