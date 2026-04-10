package com.raydo.rating_service.repository;

import com.raydo.rating_service.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    boolean existsByTripIdAndRaterId(UUID tripId, UUID raterId);

    List<Rating> findByRateeIdAndIsVisibleTrue(UUID rateeId);

}