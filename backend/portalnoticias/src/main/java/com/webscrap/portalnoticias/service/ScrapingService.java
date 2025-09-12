package com.webscrap.portalnoticias.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import com.webscrap.portalnoticias.entity.News;
import com.webscrap.portalnoticias.entity.NewsSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapingService {
    
    @Value("${scraping.timeout.seconds:30}")
    private int timeoutSeconds;
    
    @Value("${scraping.user-agent}")
    private String userAgent;
    
    private final NewsService newsService;
    
    /**
     * Scraping principal - replica exactamente la función scrape_news() de Python
     */
    public List<News> scrapeNewsSource(NewsSource source) {
        log.info("🔎 Extrayendo de {} -> {}", source.getName(), source.getBaseUrl());
        
        try {
            if (isRssSource(source)) {
                return scrapeRssSource(source);
            } else {
                return scrapeHtmlSource(source);
            }
        } catch (Exception e) {
            log.error("❌ Error en {}: {}", source.getName(), e.getMessage());
            throw new RuntimeException("Error en scraping de " + source.getName(), e);
        }
    }
    
    /**
     * Caso 1: RSS (ej. Perú21) - REPLICA EXACTA del código Python
     */
    private List<News> scrapeRssSource(NewsSource source) {
        List<News> scrapedNews = new ArrayList<>();
        
        try {
            log.info("Procesando RSS feed: {}", source.getBaseUrl());
            
            // Obtener el contenido RSS y limpiarlo (como hace Python con BeautifulSoup)
            String rssContent = getRssContentAndClean(source.getBaseUrl());
            
            // Parsear feed RSS con contenido limpio
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new ByteArrayInputStream(rssContent.getBytes("UTF-8"))));
            
            List<SyndEntry> entries = feed.getEntries();
            log.info("Entradas RSS encontradas: {}", entries.size());
            
            for (SyndEntry entry : entries) {
                try {
                    // Imagen en el <description> (EXACTAMENTE como en Python)
                    String imageUrl = null;
                    String summaryText = null;
                    
                    if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
                        String descriptionHtml = entry.getDescription().getValue();
                        Document soup = Jsoup.parse(descriptionHtml);
                        
                        // Buscar imagen (como Python: img = soup.find("img"))
                        Element img = soup.selectFirst("img");
                        if (img != null && img.hasAttr("src")) {
                            imageUrl = img.attr("src");
                        }
                        
                        // Limpiar texto del summary (como Python: soup.find_all("img").decompose())
                        soup.select("img").remove();
                        summaryText = soup.text().trim();
                    }
                    
                    // Extraer categorías (como Python: [t.term for t in e.get("tags", [])])
                    List<String> categories = new ArrayList<>();
                    if (entry.getCategories() != null) {
                        categories = entry.getCategories().stream()
                                .map(cat -> cat.getName())
                                .collect(Collectors.toList());
                    }
                    String categoryStr = String.join(", ", categories);
                    
                    // Extraer fecha (como Python)
                    LocalDateTime publishedDate = LocalDateTime.now();
                    if (entry.getPublishedDate() != null) {
                        Date pubDate = entry.getPublishedDate();
                        publishedDate = pubDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    }
                    
                    // Crear noticia (EXACTAMENTE como Python)
                    String url = entry.getLink();
                    String title = entry.getTitle();
                    String author = entry.getAuthor();
                    String guid = entry.getUri();
                    
                    // Validación (como Python: if title != null and not title.trim().isEmpty() and url != null)
                    if (title != null && !title.trim().isEmpty() && url != null) {
                        News news = News.builder()
                                .title(title.trim())
                                .content(null)
                                .summary(summaryText)
                                .originalUrl(url)
                                .imageUrl(imageUrl)
                                .source(source.getName())
                                .author(author != null ? author.trim() : "Redacción PERÚ21")
                                .category(categoryStr.isEmpty() ? "General" : categoryStr)
                                .publishedDate(publishedDate)
                                .build();
                        
                        News savedNews = newsService.saveNews(news);
                        scrapedNews.add(savedNews);
                    }
                    
                } catch (Exception e) {
                    log.warn("Error procesando entrada RSS: {}", e.getMessage());
                }
            }
            
            log.info("✅ {} noticias (RSS) de {}", scrapedNews.size(), source.getName());
            return scrapedNews;
            
        } catch (Exception e) {
            log.error("Error en RSS scraping: {}", e.getMessage());
            throw new RuntimeException("Error procesando RSS", e);
        }
    }
    
    /**
     * Obtener contenido RSS y limpiarlo - SOLUCION AL ERROR XML
     */
    private String getRssContentAndClean(String rssUrl) throws IOException {
        // Obtener contenido como hace Python con requests.get()
        Document doc = Jsoup.connect(rssUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(timeoutSeconds * 1000)
                .ignoreContentType(true)
                .get();
        
        String rssContent = doc.html();
        
        // Limpiar XML malformado (SOLUCION AL ERROR)
        String cleanedContent = cleanMalformedXml(rssContent);
        
        return cleanedContent;
    }
    
    /**
     * Caso 2: Scraping HTML - REPLICA EXACTA del código Python
     */
    private List<News> scrapeHtmlSource(NewsSource source) {
        List<News> scrapedNews = new ArrayList<>();
        
        try {
            log.info("Procesando HTML: {}", source.getBaseUrl());
            
            // Request HTTP (como Python: requests.get(source["baseUrl"], headers=headers))
            Document soup = Jsoup.connect(source.getBaseUrl())
                    .userAgent("Mozilla/5.0")
                    .timeout(timeoutSeconds * 1000)
                    .get();
            
            // Seleccionar elementos (como Python: soup.select(source["newsListSelector"]))
            Elements items = soup.select(source.getNewsListSelector());
            
            if (items.isEmpty()) {
                log.warn("No se encontraron elementos con selector: {}", source.getNewsListSelector());
                return scrapedNews;
            }
            
            log.info("Elementos HTML encontrados: {}", items.size());
            
            for (Element item : items) {
                try {
                    // Función safe_select (EXACTAMENTE como Python)
                    String title = safeSelect(item, source.getTitleSelector(), "text");
                    String summary = safeSelect(item, source.getSummarySelector(), "text");
                    String author = safeSelect(item, source.getAuthorSelector(), "text");
                    String dateText = safeSelect(item, source.getDateSelector(), "text");
                    String category = safeSelect(item, source.getCategorySelector(), "text");
                    
                    // Extraer URL (como Python: url_el = it if it.name == "a" else it.select_one("a"))
                    Element urlElement = item.tagName().equals("a") ? item : item.selectFirst("a");
                    String url = null;
                    if (urlElement != null && urlElement.hasAttr("href")) {
                        url = urlElement.attr("href");
                    }
                    
                    // Si no hay título, usar texto del enlace (como Python)
                    if ((title == null || title.trim().isEmpty()) && urlElement != null) {
                        title = urlElement.text().trim();
                    }
                    
                    // Extraer imagen (EXACTAMENTE como Python)
                    String imageUrl = null;
                    if (source.getImageSelector() != null) {
                        Element imgElement = item.selectFirst(source.getImageSelector());
                        if (imgElement != null) {
                            // Como Python: if img_el.has_attr("data-src"): image = img_el["data-src"]
                            if (imgElement.hasAttr("data-src")) {
                                imageUrl = imgElement.attr("data-src");
                            } else if (imgElement.hasAttr("src")) {
                                imageUrl = imgElement.attr("src");
                            }
                        }
                    }
                    
                    // Convertir URLs relativas a absolutas
                    if (url != null && !url.startsWith("http")) {
                        url = makeAbsoluteUrl(url, source.getBaseUrl());
                    }
                    
                    if (imageUrl != null && !imageUrl.startsWith("http")) {
                        imageUrl = makeAbsoluteUrl(imageUrl, source.getBaseUrl());
                    }
                    
                    // Parsear fecha
                    LocalDateTime publishedDate = parseDate(dateText, source.getDateFormat());
                    
                    // Crear noticia si tiene datos mínimos (como Python)
                    if (title != null && !title.trim().isEmpty() && url != null) {
                        News news = News.builder()
                                .title(title.trim())
                                .content(null)
                                .summary(summary != null ? summary.trim() : null)
                                .originalUrl(url)
                                .imageUrl(imageUrl)
                                .source(source.getName())
                                .author(author != null ? author.trim() : "Redacción")
                                .category(category != null ? category.trim() : "General")
                                .publishedDate(publishedDate)
                                .build();
                        
                        News savedNews = newsService.saveNews(news);
                        scrapedNews.add(savedNews);
                    }
                    
                } catch (Exception e) {
                    log.warn("Error procesando elemento HTML: {}", e.getMessage());
                }
            }
            
            log.info("✅ {} noticias (HTML) de {}", scrapedNews.size(), source.getName());
            return scrapedNews;
            
        } catch (IOException e) {
            log.error("❌ Error HTTP {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Error de conexión", e);
        }
    }
    
    /**
     * Función safe_select del código Python - REPLICA EXACTA
     */
    private String safeSelect(Element element, String selector, String attr) {
        if (selector == null || selector.isEmpty()) {
            return null;
        }
        
        Element targetElement = element.selectFirst(selector);
        if (targetElement == null) {
            return null;
        }
        
        if ("text".equals(attr)) {
            return targetElement.text().trim();
        } else {
            return targetElement.hasAttr(attr) ? targetElement.attr(attr) : null;
        }
    }
    
    /**
     * Convertir URL relativa a absoluta
     */
    private String makeAbsoluteUrl(String relativeUrl, String baseUrl) {
        if (relativeUrl == null || relativeUrl.startsWith("http")) {
            return relativeUrl;
        }
        
        try {
            URL base = new URL(baseUrl);
            String baseDomain = base.getProtocol() + "://" + base.getHost();
            
            if (relativeUrl.startsWith("/")) {
                return baseDomain + relativeUrl;
            } else {
                return baseDomain + "/" + relativeUrl;
            }
        } catch (Exception e) {
            return relativeUrl;
        }
    }
    
    /**
     * Parsear fecha con formato específico
     */
    private LocalDateTime parseDate(String dateText, String dateFormat) {
        if (dateText == null || dateText.trim().isEmpty() || dateFormat == null) {
            return LocalDateTime.now();
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
            return LocalDateTime.parse(dateText.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.debug("Error parseando fecha '{}': {}", dateText, e.getMessage());
            return LocalDateTime.now();
        }
    }
    
    /**
     * Verificar si es fuente RSS - COMO PYTHON
     */
    private boolean isRssSource(NewsSource source) {
        return "RSS_FEED".equals(source.getNewsListSelector()) || 
               source.getBaseUrl().contains("/feed/") ||
               source.getBaseUrl().endsWith(".rss") ||
               source.getBaseUrl().endsWith(".xml");
    }
    
    /**
     * Limpiar XML malformado - SOLUCION AL ERROR RSS
     * Corrige problemas comunes como atributos sin valores
     */
    private String cleanMalformedXml(String xmlContent) {
        if (xmlContent == null) {
            return null;
        }
        
        log.debug("Limpiando XML malformado...");
        
        // Corregir atributos sin valores (CAUSA DEL ERROR)
        String cleaned = xmlContent
                // Corregir defer, async, etc. sin valores
                .replaceAll("\\s+defer\\s*(?![=])", " defer=\"defer\" ")
                .replaceAll("\\s+async\\s*(?![=])", " async=\"async\" ")
                .replaceAll("\\s+autoplay\\s*(?![=])", " autoplay=\"autoplay\" ")
                .replaceAll("\\s+disabled\\s*(?![=])", " disabled=\"disabled\" ")
                .replaceAll("\\s+hidden\\s*(?![=])", " hidden=\"hidden\" ")
                .replaceAll("\\s+checked\\s*(?![=])", " checked=\"checked\" ")
                .replaceAll("\\s+selected\\s*(?![=])", " selected=\"selected\" ")
                .replaceAll("\\s+required\\s*(?![=])", " required=\"required\" ")
                .replaceAll("\\s+readonly\\s*(?![=])", " readonly=\"readonly\" ")
                .replaceAll("\\s+multiple\\s*(?![=])", " multiple=\"multiple\" ")
                // Remover comentarios problemáticos
                .replaceAll("<!--[\\s\\S]*?-->", "")
                // Corregir tags no cerrados comunes
                .replaceAll("<br\\s*>", "<br/>")
                .replaceAll("<hr\\s*>", "<hr/>")
                .replaceAll("<img([^>]*?)>", "<img$1/>")
                .replaceAll("<input([^>]*?)>", "<input$1/>");
        
        log.debug("XML limpiado exitosamente");
        return cleaned;
    }
}