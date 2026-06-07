package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.Appointment;
import com.agricultural.machinery.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByGrowerId(Long growerId);
    List<Appointment> findByCooperativeId(Long cooperativeId);
    List<Appointment> findByMachineId(Long machineId);
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> findByWorkDate(LocalDate workDate);
    List<Appointment> findByMachineIdAndWorkDate(Long machineId, LocalDate workDate);
}
