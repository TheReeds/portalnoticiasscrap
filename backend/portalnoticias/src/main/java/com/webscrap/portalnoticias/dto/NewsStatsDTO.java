package com.webscrap.portalnoticias.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsStatsDTO {
    private long totalNews;
    private long todayNews;
    private long thisWeekNews;
    private List<SourceStatsDTO> sourceStats;
    private List<String> categories;
}