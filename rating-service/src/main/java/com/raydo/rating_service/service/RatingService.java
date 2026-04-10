package com.raydo.rating_service.service;

import com.raydo.rating_service.dto.RatingRequestDTO;
import com.raydo.rating_service.dto.RatingSummaryDTO;
import com.raydo.rating_service.entity.Rating;
import com.raydo.rating_service.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    // ✅ 1. Submit Rating
    public void submitRating(UUID raterId, RatingRequestDTO request) {

        // 1. Validate score
        if (request.getScore() < 1 || request.getScore() > 5) {
            throw new RuntimeException("Score must be between 1 and 5");
        }

        // 2. Prevent duplicate rating
        boolean alreadyRated = ratingRepository
                .existsByTripIdAndRaterId(request.getTripId(), raterId);

        if (alreadyRated) {
            throw new RuntimeException("Rating already submitted for this trip");
        }

        // 3. Flag logic


        // 4. Build entity
        Rating rating = Rating.builder()
                .tripId(request.getTripId())
                .raterId(raterId)
                .rateeId(request.getRateeId())
                .score(request.getScore())
                .comment(request.getComment())
                .tags(request.getTags())
                .isFlagged(false)
                .isVisible(true)
                .createdAt(LocalDateTime.now())
                .build();

        // 5. Save
        ratingRepository.save(rating);
    }

    //  2. Get Rating Summary
    public RatingSummaryDTO getRatingSummary(UUID rateeId) {

        List<Rating> ratings = ratingRepository
                .findByRateeIdAndIsVisibleTrue(rateeId);

        if (ratings.isEmpty()) {
            return RatingSummaryDTO.builder()
                    .avgScore(0.0)
                    .totalCount(0)
                    .scoreDistribution(new HashMap<>())
                    .topTags(new ArrayList<>())
                    .build();
        }

        // 1. Avg score
        double avg = ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);

        // 2. Total count
        int total = ratings.size();

        // 3. Score distribution
        Map<Integer, Long> distribution = ratings.stream()
                .collect(Collectors.groupingBy(
                        Rating::getScore,
                        Collectors.counting()
                ));

        // 4. Top tags
        Map<String, Long> tagCount = new HashMap<>();

        for (Rating r : ratings) {
            if (r.getTags() != null) {
                for (String tag : r.getTags()) {
                    tagCount.put(tag, tagCount.getOrDefault(tag, 0L) + 1);
                }
            }
        }

        List<String> topTags = tagCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return RatingSummaryDTO.builder()
                .avgScore(avg)
                .totalCount(total)
                .scoreDistribution(distribution)
                .topTags(topTags)
                .build();
    }
}