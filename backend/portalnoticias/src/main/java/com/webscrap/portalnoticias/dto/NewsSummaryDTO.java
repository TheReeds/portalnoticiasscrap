package com.webscrap.portalnoticias.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsSummaryDTO {
    private Long id;
    private String title;
    private String summary;
    private String imageUrl;
    private String source;
    private String category;
    private LocalDateTime publishedDate;
    private Integer viewCount;
}
