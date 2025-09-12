package com.webscrap.portalnoticias.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_sources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 500)
    private String baseUrl;
    
    @Column(nullable = false, length = 500)
    private String newsListSelector;
    
    @Column(nullable = false, length = 200)
    private String titleSelector;
    
    @Column(length = 200)
    private String contentSelector;
    
    @Column(length = 200)
    private String summarySelector;
    
    @Column(length = 200)
    private String imageSelector;
    
    @Column(length = 200)
    private String authorSelector;
    
    @Column(length = 200)
    private String dateSelector;
    
    @Column(length = 200)
    private String categorySelector;
    
    @Column(length = 50)
    private String dateFormat;
    
    @Column
    @Builder.Default
    private Boolean isActive = true;
    
    @Column
    @Builder.Default
    private Integer scrapingIntervalMinutes = 30;
    
    @Column
    private LocalDateTime lastScrapedAt;
    
    @Column
    @Builder.Default
    private Integer successfulScrapes = 0;
    
    @Column
    @Builder.Default
    private Integer failedScrapes = 0;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Métodos de utilidad
    public void incrementSuccessfulScrapes() {
        this.successfulScrapes++;
        this.lastScrapedAt = LocalDateTime.now();
    }
    
    public void incrementFailedScrapes() {
        this.failedScrapes++;
        this.lastScrapedAt = LocalDateTime.now();
    }
}