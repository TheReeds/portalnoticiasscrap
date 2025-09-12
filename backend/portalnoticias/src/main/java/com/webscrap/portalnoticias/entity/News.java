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
@Table(name = "news", 
       indexes = {
           @Index(name = "idx_news_published_date", columnList = "publishedDate"),
           @Index(name = "idx_news_source", columnList = "source"),
           @Index(name = "idx_news_category", columnList = "category"),
           @Index(name = "idx_news_url", columnList = "originalUrl", unique = true)
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(length = 1000)
    private String summary;
    
    @Column(nullable = false, unique = true, length = 1000)
    private String originalUrl;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(nullable = false, length = 100)
    private String source;
    
    @Column(length = 100)
    private String author;
    
    @Column(length = 100)
    private String category;
    
    @Column
    private LocalDateTime publishedDate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    @Builder.Default
    private Boolean isActive = true;
    
    @Column
    @Builder.Default
    private Integer viewCount = 0;
    
    // Método para incrementar vistas
    public void incrementViewCount() {
        this.viewCount = this.viewCount + 1;
    }
}