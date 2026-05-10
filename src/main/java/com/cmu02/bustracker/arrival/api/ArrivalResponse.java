package com.cmu02.bustracker.arrival.api;

import java.util.List;

/**
 * GET /api/v1/routes/{routeId}/stations/{stationId}/arrivals 응답 DTO.
 */
public record ArrivalResponse(
        String routeId,
        String stationId,
        List<ArrivalSlot> arrivals
) {
}
