package com.raydo.rating_service;

import com.raydo.rating_service.dto.RatingRequestDTO;
import com.raydo.rating_service.dto.RatingSummaryDTO;
import com.raydo.rating_service.entity.Rating;
import com.raydo.rating_service.repository.RatingRepository;
import com.raydo.rating_service.service.RatingService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RatingService Tests")
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    private UUID raterId;
    private UUID rateeId;
    private UUID tripId;

    @BeforeEach
    void setUp() {
        raterId = UUID.randomUUID();
        rateeId = UUID.randomUUID();
        tripId  = UUID.randomUUID();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // submitRating
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("submitRating()")
    class SubmitRating {

        private RatingRequestDTO validRequest() {
            RatingRequestDTO req = new RatingRequestDTO();
            req.setTripId(tripId);
            req.setRateeId(rateeId);
            req.setScore(5);
            req.setComment("Great ride!");
            req.setTags(List.of("polite", "clean"));
            return req;
        }

        @Test
        @DisplayName("saves rating successfully for valid input")
        void savesRating_forValidInput() {
            RatingRequestDTO req = validRequest();
            when(ratingRepository.existsByTripIdAndRaterId(tripId, raterId)).thenReturn(false);

            ratingService.submitRating(raterId, req);

            verify(ratingRepository).save(argThat(r ->
                    r.getScore()    == 5 &&
                            r.getRaterId().equals(raterId) &&
                            r.getRateeId().equals(rateeId) &&
                            r.getTripId().equals(tripId) &&
                            !r.isFlagged() &&
                            r.isVisible()
            ));
        }

        @Test
        @DisplayName("throws RuntimeException for score below 1")
        void throwsForScoreBelow1() {
            RatingRequestDTO req = validRequest();
            req.setScore(0);

            assertThatThrownBy(() -> ratingService.submitRating(raterId, req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Score must be between 1 and 5");

            verify(ratingRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws RuntimeException for score above 5")
        void throwsForScoreAbove5() {
            RatingRequestDTO req = validRequest();
            req.setScore(6);

            assertThatThrownBy(() -> ratingService.submitRating(raterId, req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Score must be between 1 and 5");
        }

        @Test
        @DisplayName("throws RuntimeException when rating already submitted")
        void throwsForDuplicateRating() {
            RatingRequestDTO req = validRequest();
            when(ratingRepository.existsByTripIdAndRaterId(tripId, raterId)).thenReturn(true);

            assertThatThrownBy(() -> ratingService.submitRating(raterId, req))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already submitted");

            verify(ratingRepository, never()).save(any());
        }

        @Test
        @DisplayName("accepts boundary score of 1")
        void acceptsBoundaryScore1() {
            RatingRequestDTO req = validRequest();
            req.setScore(1);
            when(ratingRepository.existsByTripIdAndRaterId(tripId, raterId)).thenReturn(false);

            assertThatCode(() -> ratingService.submitRating(raterId, req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("accepts boundary score of 5")
        void acceptsBoundaryScore5() {
            RatingRequestDTO req = validRequest();
            req.setScore(5);
            when(ratingRepository.existsByTripIdAndRaterId(tripId, raterId)).thenReturn(false);

            assertThatCode(() -> ratingService.submitRating(raterId, req))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("saves rating with null tags gracefully")
        void savesWithNullTags() {
            RatingRequestDTO req = validRequest();
            req.setTags(null);
            when(ratingRepository.existsByTripIdAndRaterId(tripId, raterId)).thenReturn(false);

            assertThatCode(() -> ratingService.submitRating(raterId, req))
                    .doesNotThrowAnyException();

            verify(ratingRepository).save(argThat(r -> r.getTags() == null));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getRatingSummary
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getRatingSummary()")
    class GetRatingSummary {

        private Rating buildRating(int score, List<String> tags) {
            return Rating.builder()
                    .tripId(UUID.randomUUID())
                    .raterId(UUID.randomUUID())
                    .rateeId(rateeId)
                    .score(score)
                    .tags(tags)
                    .isFlagged(false)
                    .isVisible(true)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("returns empty summary when no ratings exist")
        void returnsEmptySummary_whenNoRatings() {
            when(ratingRepository.findByRateeIdAndIsVisibleTrue(rateeId))
                    .thenReturn(Collections.emptyList());

            RatingSummaryDTO result = ratingService.getRatingSummary(rateeId);

            assertThat(result.getAvgScore()).isEqualTo(0.0);
            assertThat(result.getTotalCount()).isEqualTo(0);
            assertThat(result.getScoreDistribution()).isEmpty();
            assertThat(result.getTopTags()).isEmpty();
        }

        @Test
        @DisplayName("calculates correct average score")
        void calculatesCorrectAvgScore() {
            List<Rating> ratings = List.of(
                    buildRating(4, null),
                    buildRating(5, null),
                    buildRating(3, null)
            );
            when(ratingRepository.findByRateeIdAndIsVisibleTrue(rateeId)).thenReturn(ratings);

            RatingSummaryDTO result = ratingService.getRatingSummary(rateeId);

            assertThat(result.getAvgScore()).isEqualTo(4.0);
            assertThat(result.getTotalCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("returns correct score distribution")
        void returnsCorrectScoreDistribution() {
            List<Rating> ratings = List.of(
                    buildRating(5, null),
                    buildRating(5, null),
                    buildRating(4, null)
            );
            when(ratingRepository.findByRateeIdAndIsVisibleTrue(rateeId)).thenReturn(ratings);

            RatingSummaryDTO result = ratingService.getRatingSummary(rateeId);

            assertThat(result.getScoreDistribution()).containsEntry(5, 2L);
            assertThat(result.getScoreDistribution()).containsEntry(4, 1L);
        }

        @Test
        @DisplayName("returns top 5 tags sorted by frequency")
        void returnsTopTagsSortedByFrequency() {
            List<Rating> ratings = List.of(
                    buildRating(5, List.of("polite", "fast", "clean")),
                    buildRating(5, List.of("polite", "fast")),
                    buildRating(4, List.of("polite"))
            );
            when(ratingRepository.findByRateeIdAndIsVisibleTrue(rateeId)).thenReturn(ratings);

            RatingSummaryDTO result = ratingService.getRatingSummary(rateeId);

            assertThat(result.getTopTags().get(0)).isEqualTo("polite"); // 3 times
            assertThat(result.getTopTags().get(1)).isEqualTo("fast");   // 2 times
        }

        @Test
        @DisplayName("limits top tags to maximum 5")
        void limitsTopTagsToFive() {
            List<String> manyTags = List.of("a", "b", "c", "d", "e", "f", "g");
            List<Rating> ratings = List.of(buildRating(5, manyTags));
            when(ratingRepository.findByRateeIdAndIsVisibleTrue(rateeId)).thenReturn(ratings);

            RatingSummaryDTO result = ratingService.getRatingSummary(rateeId);

            assertThat(result.getTopTags()).hasSizeLessThanOrEqualTo(5);
        }

        @Test
        @DisplayName("handles ratings with null tags without NPE")
        void handlesNullTagsGracefully() {
            List<Rating> ratings = List.of(
                    buildRating(5, null),
                    buildRating(4, List.of("fast"))
            );
            when(ratingRepository.findByRateeIdAndIsVisibleTrue(rateeId)).thenReturn(ratings);

            assertThatCode(() -> ratingService.getRatingSummary(rateeId))
                    .doesNotThrowAnyException();
        }
    }
}