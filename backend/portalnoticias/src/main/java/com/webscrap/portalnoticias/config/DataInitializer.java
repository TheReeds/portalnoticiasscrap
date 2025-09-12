package com.webscrap.portalnoticias.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.webscrap.portalnoticias.entity.News;
import com.webscrap.portalnoticias.entity.NewsSource;
import com.webscrap.portalnoticias.service.NewsSourceService;
import com.webscrap.portalnoticias.service.ScrapingService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final NewsSourceService newsSourceService;
    private final ScrapingService scrapingService;
    
    @Value("${scraping.initial.enabled:true}")
    private boolean initialScrapingEnabled;
    
    @Value("${scraping.initial.delay.seconds:10}")
    private int initialDelaySeconds;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Inicializando datos básicos...");
        
        boolean sourcesCreated = false;
        
        // Verificar si ya existen fuentes
        if (newsSourceService.getAllSources().isEmpty()) {
            createPythonValidatedSources();
            sourcesCreated = true;
        } else {
            log.info("Fuentes de noticias ya existen, saltando inicialización");
        }
        
        // Realizar scraping inicial si está habilitado
        if (initialScrapingEnabled) {
            Thread.sleep(initialDelaySeconds * 1000);
            performInitialScraping(sourcesCreated);
        } else {
            log.info("Scraping inicial deshabilitado por configuración");
        }
    }
    
    /**
     * Crear las EXACTAS fuentes que funcionaron en Python
     */
    private void createPythonValidatedSources() {
        log.info("=== CREANDO FUENTES VALIDADAS CON PYTHON ===");
        
        // ===============================
        // 1. Perú21 - RSS Feed (isRss: True)
        // ===============================
        NewsSource peru21 = NewsSource.builder()
                .name("Perú21")
                .baseUrl("https://peru21.pe/feed/")
                .newsListSelector("RSS_FEED") // Marcador especial para RSS
                .titleSelector("title") // Para RSS
                .summarySelector("summary") // Para RSS
                .imageSelector("img") // Buscar en description
                .authorSelector("author") // Para RSS
                .dateSelector("published") // Para RSS
                .categorySelector("tags") // Para RSS
                .dateFormat("EEE, dd MMM yyyy HH:mm:ss Z") // Formato RSS estándar
                .scrapingIntervalMinutes(30)
                .isActive(true)
                .build();
        
        // ===============================
        // 2. Gestión - HTML Scraping
        // ===============================
        NewsSource gestion = NewsSource.builder()
                .name("Gestión")
                .baseUrl("https://gestion.pe/economia")
                .newsListSelector(".story-item") // EXACTO como Python
                .titleSelector(".story-item__title") // EXACTO como Python
                .summarySelector(".story-item__subtitle") // EXACTO como Python
                .imageSelector("img") // EXACTO como Python
                .authorSelector(".story-item__author") // EXACTO como Python
                .dateSelector(".story-item__date-time") // EXACTO como Python
                .categorySelector(".story-item__section") // EXACTO como Python
                .dateFormat("dd/MM/yyyy HH:mm")
                .scrapingIntervalMinutes(45)
                .isActive(true)
                .build();
        
        // ===============================
        // 3. El País - HTML Scraping
        // ===============================
        NewsSource elPais = NewsSource.builder()
                .name("El País")
                .baseUrl("https://elpais.com/ultimas-noticias/")
                .newsListSelector("article") // EXACTO como Python
                .titleSelector("h2 a, h3 a") // EXACTO como Python
                .summarySelector("p") // EXACTO como Python
                .imageSelector("img") // EXACTO como Python
                .authorSelector(".autor, .byline") // EXACTO como Python
                .dateSelector("time") // EXACTO como Python
                .categorySelector(".section, .kicker") // EXACTO como Python
                .dateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .scrapingIntervalMinutes(60)
                .isActive(true)
                .build();
        
        try {
            newsSourceService.saveSource(peru21);
            newsSourceService.saveSource(gestion);
            newsSourceService.saveSource(elPais);
            
            log.info("=== FUENTES PYTHON VALIDADAS CREADAS ===");
            log.info("✅ 1. Perú21 - RSS Feed");
            log.info("   URL: https://peru21.pe/feed/");
            log.info("   Tipo: RSS (isRss: True)");
            log.info("   Validado: ✅ 20 noticias en Python");
            log.info("");
            log.info("✅ 2. Gestión - HTML Scraping");
            log.info("   URL: https://gestion.pe/economia");
            log.info("   Selector: .story-item");
            log.info("   Validado: ✅ 50 noticias en Python");
            log.info("");
            log.info("✅ 3. El País - HTML Scraping");
            log.info("   URL: https://elpais.com/ultimas-noticias/");
            log.info("   Selector: article");
            log.info("   Validado: ✅ 84 noticias en Python");
            log.info("==========================================");
            
        } catch (Exception e) {
            log.error("Error creando fuentes validadas: {}", e.getMessage());
        }
    }
    
    /**
     * Realizar scraping inicial con las fuentes validadas
     */
    private void performInitialScraping(boolean sourcesJustCreated) {
        try {
            List<NewsSource> activeSources = newsSourceService.getActiveSources();
            
            if (activeSources.isEmpty()) {
                log.warn("No hay fuentes activas para scraping inicial");
                return;
            }
            
            log.info("=== INICIANDO SCRAPING INICIAL (JAVA vs PYTHON) ===");
            log.info("Objetivo: Replicar exactamente los resultados de Python");
            log.info("Esperado: Perú21(20) + Gestión(50) + El País(84) = 154 noticias");
            log.info("======================================================");
            
            int totalNewsScraped = 0;
            int successfulSources = 0;
            
            for (NewsSource source : activeSources) {
                try {
                    String sourceType = isRssSource(source) ? "RSS" : "HTML";
                    log.info("🔄 Procesando {} ({}): {}", source.getName(), sourceType, source.getBaseUrl());
                    
                    long startTime = System.currentTimeMillis();
                    List<News> scrapedNews = scrapingService.scrapeNewsSource(source);
                    long endTime = System.currentTimeMillis();
                    
                    totalNewsScraped += scrapedNews.size();
                    newsSourceService.updateSourceStats(source.getId(), true, scrapedNews.size());
                    successfulSources++;
                    
                    // Comparar con resultados de Python
                    int expectedCount = getExpectedCountFromPython(source.getName());
                    String comparison = scrapedNews.size() >= expectedCount ? "✅ BIEN" : "⚠️ MENOS";
                    
                    log.info("{} {}: {} noticias (Python: {}) - {} en {}ms", 
                            comparison, source.getName(), scrapedNews.size(), 
                            expectedCount, sourceType, (endTime - startTime));
                    
                    // Pausa entre fuentes para no sobrecargar
                    Thread.sleep(3000);
                    
                } catch (Exception e) {
                    log.error("❌ Error en {}: {}", source.getName(), e.getMessage());
                    newsSourceService.updateSourceStats(source.getId(), false, 0);
                }
            }
            
            log.info("=== SCRAPING INICIAL COMPLETADO ===");
            log.info("Total extraído: {} noticias", totalNewsScraped);
            log.info("Fuentes exitosas: {}/{}", successfulSources, activeSources.size());
            
            // Evaluación final comparando con Python
            evaluateAgainstPython(totalNewsScraped, successfulSources);
            
        } catch (Exception e) {
            log.error("Error general en scraping inicial: {}", e.getMessage());
        }
    }
    
    /**
     * Obtener conteo esperado basado en resultados de Python
     */
    private int getExpectedCountFromPython(String sourceName) {
        switch (sourceName) {
            case "Perú21": return 20;  // RSS - Python extrajo 20
            case "Gestión": return 50; // HTML - Python extrajo 50  
            case "El País": return 84; // HTML - Python extrajo 84
            default: return 10;
        }
    }
    
    /**
     * Evaluar resultados comparando con Python
     */
    private void evaluateAgainstPython(int totalNews, int successfulSources) {
        int pythonTotal = 154; // 20 + 50 + 84
        double successRate = (double) totalNews / pythonTotal * 100;
        
        log.info("=== EVALUACIÓN vs PYTHON ===");
        log.info("Python extrajo: {} noticias", pythonTotal);
        log.info("Java extrajo: {} noticias", totalNews);
        log.info("Tasa de éxito: {:.1f}%", successRate);
        
        if (successRate >= 80) {
            log.info("🎉 EXCELENTE! Replicación exitosa del scraping Python");
            log.info("🚀 Portal de noticias listo! API disponible en /api/news");
        } else if (successRate >= 50) {
            log.info("⚠️ PARCIAL: Funciona pero hay diferencias con Python");
            log.info("🔧 Revisar selectores HTML o conectividad RSS");
        } else {
            log.warn("❌ PROBLEMA: Resultados muy diferentes a Python");
            log.warn("🛠️ Verificar implementación y conectividad");
        }
        
        log.info("===========================");
    }
    
    /**
     * Verificar si una fuente es RSS (como en Python isRss: True)
     */
    private boolean isRssSource(NewsSource source) {
        return "RSS_FEED".equals(source.getNewsListSelector()) || 
               source.getBaseUrl().contains("/feed/") ||
               source.getBaseUrl().endsWith(".rss") ||
               source.getBaseUrl().endsWith(".xml");
    }
}