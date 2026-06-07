package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.TrajectoryPoint;
import com.agricultural.machinery.repository.TrajectoryPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrajectoryService {

    private final TrajectoryPointRepository trajectoryPointRepository;

    public List<TrajectoryPoint> getTrajectoryByWorkOrderId(Long workOrderId) {
        return trajectoryPointRepository.findByWorkOrderIdOrderByTimestamp(workOrderId);
    }

    @Transactional
    public TrajectoryPoint addTrajectoryPoint(TrajectoryPoint point) {
        return trajectoryPointRepository.save(point);
    }

    @Transactional
    public List<TrajectoryPoint> addTrajectoryPoints(List<TrajectoryPoint> points) {
        return trajectoryPointRepository.saveAll(points);
    }
}
