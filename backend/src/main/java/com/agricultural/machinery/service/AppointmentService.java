package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.Appointment;
import com.agricultural.machinery.entity.Field;
import com.agricultural.machinery.entity.Machine;
import com.agricultural.machinery.entity.User;
import com.agricultural.machinery.enums.AppointmentStatus;
import com.agricultural.machinery.repository.AppointmentRepository;
import com.agricultural.machinery.repository.FieldRepository;
import com.agricultural.machinery.repository.MachineRepository;
import com.agricultural.machinery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final FieldRepository fieldRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
    }

    public List<Appointment> getAppointmentsByGrowerId(Long growerId) {
        return appointmentRepository.findByGrowerId(growerId);
    }

    public List<Appointment> getAppointmentsByCooperativeId(Long cooperativeId) {
        return appointmentRepository.findByCooperativeId(cooperativeId);
    }

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        Field field = fieldRepository.findById(appointment.getFieldId())
                .orElseThrow(() -> new RuntimeException("地块不存在"));
        
        appointment.setAppointmentNo("APT" + System.currentTimeMillis());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setGrowerId(field.getGrowerId());
        
        if (appointment.getEstimatedArea() == null) {
            appointment.setEstimatedArea(field.getArea());
        }
        
        if (appointment.getEstimatedCost() == null && appointment.getMachineId() != null) {
            Machine machine = machineRepository.findById(appointment.getMachineId()).orElse(null);
            if (machine != null && machine.getPricePerMu() != null) {
                appointment.setEstimatedCost(machine.getPricePerMu().multiply(appointment.getEstimatedArea()));
            }
        }
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment scheduleAppointment(Long id, Long cooperativeId, Long machineId, Long operatorId) {
        Appointment appointment = getAppointmentById(id);
        
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("机具不存在"));
        
        User operator = userRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("农机手不存在"));
        
        List<Appointment> conflicting = appointmentRepository
                .findByMachineIdAndWorkDate(machineId, appointment.getWorkDate());
        
        for (Appointment apt : conflicting) {
            if (apt.getId().equals(id)) continue;
            if (isTimeOverlap(apt.getStartTime(), apt.getEndTime(),
                    appointment.getStartTime(), appointment.getEndTime())) {
                throw new RuntimeException("该机具在该时间段已有预约");
            }
        }
        
        appointment.setCooperativeId(cooperativeId);
        appointment.setMachineId(machineId);
        appointment.setOperatorId(operatorId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment handleRainDelay(Long id, LocalDate newDate, String reason) {
        Appointment appointment = getAppointmentById(id);
        
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED &&
            appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("当前状态不支持雨后改期");
        }
        
        appointment.setStatus(AppointmentStatus.RAIN_DELAYED);
        appointment.setRainDelayToDate(newDate);
        appointment.setRainDelayReason(reason);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment rescheduleAfterRain(Long id, LocalDate newDate, LocalTime startTime, LocalTime endTime) {
        Appointment appointment = getAppointmentById(id);
        
        if (appointment.getStatus() != AppointmentStatus.RAIN_DELAYED) {
            throw new RuntimeException("只有雨后延期的预约才能重新排程");
        }
        
        appointment.setWorkDate(newDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment cancelAppointment(Long id, String reason) {
        Appointment appointment = getAppointmentById(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setRemark(reason);
        return appointmentRepository.save(appointment);
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByWorkDate(date);
    }
}
