package com.rydo.location_service.utility;

import java.util.Map;

public record ErrorResponse(
        int status,
        String message,
        Map<String, String> errors, // For field-specific validation errors
        long timestamp
) {}