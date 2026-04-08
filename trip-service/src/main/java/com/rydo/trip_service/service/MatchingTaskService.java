package com.rydo.trip_service.service;

import com.rydo.trip_service.clients.MatchingServiceClient;
import com.rydo.trip_service.dto.RiderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchingTaskService {
    private final MatchingServiceClient matchingClient;

    @Async
    public void triggerMatching(RiderDTO request) {
        try {
            // This happens in a background thread
            matchingClient.searchDrivers(request);
        } catch (Exception e) {
            // Log error so the trip isn't lost, 
            // maybe retry or update trip status to 'MATCHING_FAILED'
        }
    }
}