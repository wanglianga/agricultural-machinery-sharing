package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.TrajectoryPoint;
import com.agricultural.machinery.service.TrajectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trajectory")
@RequiredArgsConstructor
public class TrajectoryController {

    private final TrajectoryService trajectoryService;

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<List<TrajectoryPoint>> getTrajectoryByWorkOrderId(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(trajectoryService.getTrajectoryByWorkOrderId(workOrderId));
    }

    @PostMapping
    public ResponseEntity<TrajectoryPoint> addTrajectoryPoint(@RequestBody TrajectoryPoint point) {
        return ResponseEntity.ok(trajectoryService.addTrajectoryPoint(point));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TrajectoryPoint>> addTrajectoryPoints(@RequestBody List<TrajectoryPoint> points) {
        return ResponseEntity.ok(trajectoryService.addTrajectoryPoints(points));
    }
}
