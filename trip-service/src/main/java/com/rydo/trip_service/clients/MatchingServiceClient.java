package com.rydo.trip_service.clients;

import com.rydo.trip_service.dto.RiderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// "matching-service" must match the name in application.yml or Eureka
@FeignClient(name = "matching-service", url = "http://localhost:8081")
public interface MatchingServiceClient {

    @PostMapping("/api/v1/matching/find-drivers")
    void searchDrivers(@RequestBody RiderDTO request);
}