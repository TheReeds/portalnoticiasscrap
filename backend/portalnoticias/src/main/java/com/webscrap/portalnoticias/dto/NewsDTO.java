package com.webscrap.portalnoticias.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDTO {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String originalUrl;
    private String imageUrl;
    private String source;
    private String author;
    private String category;
    private LocalDateTime publishedDate;
    private LocalDateTime createdAt;
    private Integer viewCount;
}




