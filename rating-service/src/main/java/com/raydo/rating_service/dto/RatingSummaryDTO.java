package com.raydo.rating_service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class RatingSummaryDTO {

    private double avgScore;
    private int totalCount;
    private Map<Integer, Long> scoreDistribution;
    private List<String> topTags;
}