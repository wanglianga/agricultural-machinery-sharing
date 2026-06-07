package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.TrajectoryPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrajectoryPointRepository extends JpaRepository<TrajectoryPoint, Long> {
    List<TrajectoryPoint> findByWorkOrderIdOrderByTimestamp(Long workOrderId);
}
