package com.webscrap.portalnoticias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSourceCreateDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    private String name;
    
    @NotBlank(message = "La URL base es obligatoria")
    @Pattern(regexp = "^https?://.*", message = "La URL debe comenzar con http:// o https://")
    private String baseUrl;
    
    @NotBlank(message = "El selector de lista de noticias es obligatorio")
    private String newsListSelector;
    
    @NotBlank(message = "El selector de título es obligatorio") 
    private String titleSelector;
    
    private String contentSelector;
    
    private String summarySelector;
    
    private String imageSelector;
    
    private String authorSelector;
    
    private String dateSelector;
    
    private String categorySelector;
    
    private String dateFormat;
    
    private Boolean isActive = true;
    
    @Min(value = 1, message = "El intervalo debe ser al menos 1 minuto")
    private Integer scrapingIntervalMinutes = 30;
}