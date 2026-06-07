package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.RescheduleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RescheduleRecordRepository extends JpaRepository<RescheduleRecord, Long> {

    List<RescheduleRecord> findByAppointmentId(Long appointmentId);

    List<RescheduleRecord> findByRescheduleType(String rescheduleType);
}
