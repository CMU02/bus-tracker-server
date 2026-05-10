package com.cmu02.bustracker.arrival.api;

import com.cmu02.bustracker.arrival.application.ArrivalService;
import com.cmu02.bustracker.arrival.domain.BusArrival;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 정류소 도착 예정 조회 컨트롤러.
 * GET /api/v1/routes/{routeId}/stations/{stationId}/arrivals
 */
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class ArrivalController {

    private final ArrivalService arrivalService;

    @GetMapping("/{routeId}/stations/{stationId}/arrivals")
    public ResponseEntity<ArrivalResponse> getArrivals(
            @PathVariable String routeId,
            @PathVariable String stationId) {

        List<BusArrival> arrivals = arrivalService.getArrivals(routeId, stationId);

        List<ArrivalSlot> slots = arrivals.stream()
                .map(a -> new ArrivalSlot(
                        a.slot(),
                        a.vehicleId(),
                        a.licensePlate(),
                        a.arrivalMessage(),
                        a.stopsAway(),
                        a.busType().name()
                ))
                .toList();

        return ResponseEntity.ok(new ArrivalResponse(routeId, stationId, slots));
    }
}
