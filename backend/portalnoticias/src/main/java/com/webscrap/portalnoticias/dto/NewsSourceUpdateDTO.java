package com.webscrap.portalnoticias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSourceUpdateDTO {
    
    private String name;
    
    @Pattern(regexp = "^https?://.*", message = "La URL debe comenzar con http:// o https://")
    private String baseUrl;
    
    private String newsListSelector;
    private String titleSelector;
    private String contentSelector;
    private String summarySelector;
    private String imageSelector;
    private String authorSelector;
    private String dateSelector;
    private String categorySelector;
    
    // NUEVOS SELECTORES
    private String linkSelector;
    private String fullContentSelector;
    private String fullImageSelector;
    private String fullAuthorSelector;
    private String fullDateSelector;
    private String fullCategorySelector;
    private String tagsSelector;
    private String sourceUrlSelector;
    private String videoSelector;
    private String gallerySelector;
    private String relatedNewsSelector;
    
    private String dateFormat;
    private Boolean isActive;
    
    @Min(value = 1, message = "El intervalo debe ser al menos 1 minuto")
    private Integer scrapingIntervalMinutes;
    
    // NUEVAS CONFIGURACIONES
    private Boolean enableFullContentScraping;
    private Integer maxContentLength;
    private Boolean removeAds;
    private List<String> adSelectorsToRemove;
    private Boolean extractMetadata;
}