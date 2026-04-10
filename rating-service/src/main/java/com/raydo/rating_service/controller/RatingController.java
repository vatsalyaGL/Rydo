package com.raydo.rating_service.controller;

import com.raydo.rating_service.dto.RatingRequestDTO;
import com.raydo.rating_service.dto.RatingSummaryDTO;
import com.raydo.rating_service.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // POST /ratings → Submit rating
    @PostMapping
    public ResponseEntity<String> submitRating(
            @RequestHeader("X-User-Id") UUID raterId,
            @RequestBody RatingRequestDTO request
    ) {

        ratingService.submitRating(raterId, request);

        return ResponseEntity.status(201).body("Rating submitted successfully");
    }

    // GET /ratings/user/{id} → Get summary
    @GetMapping("/user/{id}")
    public ResponseEntity<RatingSummaryDTO> getRatingSummary(
            @PathVariable UUID id
    ) {

        RatingSummaryDTO response = ratingService.getRatingSummary(id);

        return ResponseEntity.ok(response);
    }
}