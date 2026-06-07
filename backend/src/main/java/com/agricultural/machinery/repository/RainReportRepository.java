package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.RainReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RainReportRepository extends JpaRepository<RainReport, Long> {

    List<RainReport> findByFieldId(Long fieldId);

    List<RainReport> findByAppointmentId(Long appointmentId);

    List<RainReport> findByOperatorId(Long operatorId);

    List<RainReport> findByMachineId(Long machineId);
}
