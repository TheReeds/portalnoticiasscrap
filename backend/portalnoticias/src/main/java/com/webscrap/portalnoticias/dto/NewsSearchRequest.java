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
public class NewsSearchRequest {
    private String keyword;
    private String source;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page = 0;
    private int size = 20;
    private String sortBy = "publishedDate";
    private String sortDirection = "desc";
}