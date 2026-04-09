package com.raydo.rating_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tripId", "raterId"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID tripId;

    @Column(nullable = false)
    private UUID raterId;

    @Column(nullable = false)
    private UUID rateeId;

    @Column(nullable = false)
    private int score;

    @Column(length = 500)
    private String comment;

    @ElementCollection
    private List<String> tags;

    private boolean isFlagged;

    private boolean isVisible;

    private LocalDateTime createdAt;
}