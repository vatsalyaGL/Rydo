package com.raydo.rating_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RatingRequestDTO {

    private UUID tripId;
    private UUID rateeId;
    private int score;
    private String comment;
    private List<String> tags;
}