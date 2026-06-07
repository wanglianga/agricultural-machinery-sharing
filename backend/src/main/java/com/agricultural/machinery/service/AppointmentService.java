package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.Appointment;
import com.agricultural.machinery.entity.Field;
import com.agricultural.machinery.entity.Machine;
import com.agricultural.machinery.entity.RescheduleRecord;
import com.agricultural.machinery.entity.User;
import com.agricultural.machinery.enums.AppointmentStatus;
import com.agricultural.machinery.repository.AppointmentRepository;
import com.agricultural.machinery.repository.FieldRepository;
import com.agricultural.machinery.repository.MachineRepository;
import com.agricultural.machinery.repository.RescheduleRecordRepository;
import com.agricultural.machinery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final FieldRepository fieldRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;
    private final RescheduleRecordRepository rescheduleRecordRepository;

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

    public List<RescheduleRecord> getRescheduleRecordsByAppointmentId(Long appointmentId) {
        return rescheduleRecordRepository.findByAppointmentId(appointmentId);
    }

    public List<RescheduleRecord> getRescheduleRecordsByGrowerId(Long growerId) {
        List<Appointment> appointments = appointmentRepository.findByGrowerId(growerId);
        List<RescheduleRecord> records = new ArrayList<>();
        for (Appointment apt : appointments) {
            records.addAll(rescheduleRecordRepository.findByAppointmentId(apt.getId()));
        }
        records.sort(Comparator.comparing(RescheduleRecord::getCreatedAt).reversed());
        return records;
    }

    @Transactional
    public Appointment rescheduleAfterRain(Long id, LocalDate newDate, LocalTime startTime, LocalTime endTime, Long rescheduledBy) {
        Appointment appointment = getAppointmentById(id);
        
        if (appointment.getStatus() != AppointmentStatus.RAIN_DELAYED) {
            throw new RuntimeException("只有雨后延期的预约才能重新排程");
        }
        
        RescheduleRecord record = new RescheduleRecord();
        record.setAppointmentId(appointment.getId());
        record.setRescheduleType("RAIN_RESCHEDULE");
        record.setOriginalWorkDate(appointment.getWorkDate());
        record.setOriginalStartTime(appointment.getStartTime());
        record.setOriginalEndTime(appointment.getEndTime());
        record.setOriginalStatus(appointment.getStatus());
        record.setNewWorkDate(newDate);
        record.setNewStartTime(startTime);
        record.setNewEndTime(endTime);
        record.setReason("雨后重新排程");
        record.setRescheduledBy(rescheduledBy);
        rescheduleRecordRepository.save(record);
        
        appointment.setWorkDate(newDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Map<String, Object> rescheduleRainDelayedAppointments(Long cooperativeId, LocalDate fromDate) {
        List<Appointment> rainDelayed = appointmentRepository.findByStatus(AppointmentStatus.RAIN_DELAYED);
        List<Appointment> rescheduled = new ArrayList<>();
        List<Appointment> advanced = new ArrayList<>();

        for (Appointment apt : rainDelayed) {
            if (apt.getCooperativeId() != null && apt.getCooperativeId().equals(cooperativeId)) {
                LocalDate newDate = fromDate;
                boolean scheduled = false;
                
                while (!scheduled && !newDate.isAfter(fromDate.plusDays(7))) {
                    List<Appointment> dayAppointments = appointmentRepository
                            .findByMachineIdAndWorkDate(apt.getMachineId(), newDate);
                    
                    if (dayAppointments.isEmpty()) {
                        apt.setWorkDate(newDate);
                        apt.setStatus(AppointmentStatus.SCHEDULED);
                        appointmentRepository.save(apt);
                        
                        RescheduleRecord record = new RescheduleRecord();
                        record.setAppointmentId(apt.getId());
                        record.setRescheduleType("AUTO_RESCHEDULE");
                        record.setOriginalWorkDate(apt.getRainDelayToDate() != null ? apt.getRainDelayToDate() : apt.getWorkDate());
                        record.setOriginalStatus(AppointmentStatus.RAIN_DELAYED);
                        record.setNewWorkDate(newDate);
                        record.setNewStartTime(apt.getStartTime());
                        record.setNewEndTime(apt.getEndTime());
                        record.setReason("系统自动重新排程");
                        record.setRescheduledBy(cooperativeId);
                        rescheduleRecordRepository.save(record);
                        
                        rescheduled.add(apt);
                        scheduled = true;
                    } else {
                        newDate = newDate.plusDays(1);
                    }
                }
            }
        }

        List<Appointment> pendingAppointments = appointmentRepository.findByStatus(AppointmentStatus.PENDING);
        for (Appointment pendingApt : pendingAppointments) {
            if (pendingApt.getCooperativeId() != null && pendingApt.getCooperativeId().equals(cooperativeId)) {
                Field pendingField = fieldRepository.findById(pendingApt.getFieldId()).orElse(null);
                if (pendingField != null && pendingField.getVillage() != null) {
                    for (Appointment rainApt : rainDelayed) {
                        if (rainApt.getMachineId() != null && rainApt.getCooperativeId() != null 
                                && rainApt.getCooperativeId().equals(cooperativeId)) {
                            Field rainField = fieldRepository.findById(rainApt.getFieldId()).orElse(null);
                            if (rainField != null && pendingField.getVillage().equals(rainField.getVillage())) {
                                LocalDate vacantDate = rainApt.getWorkDate();
                                List<Appointment> dayApts = appointmentRepository
                                        .findByMachineIdAndWorkDate(rainApt.getMachineId(), vacantDate);
                                
                                boolean hasOverlap = false;
                                for (Appointment a : dayApts) {
                                    if (isTimeOverlap(a.getStartTime(), a.getEndTime(),
                                            pendingApt.getStartTime(), pendingApt.getEndTime())) {
                                        hasOverlap = true;
                                        break;
                                    }
                                }
                                
                                if (!hasOverlap) {
                                    pendingApt.setMachineId(rainApt.getMachineId());
                                    pendingApt.setOperatorId(rainApt.getOperatorId());
                                    pendingApt.setWorkDate(vacantDate);
                                    pendingApt.setStatus(AppointmentStatus.SCHEDULED);
                                    appointmentRepository.save(pendingApt);
                                    
                                    RescheduleRecord record = new RescheduleRecord();
                                    record.setAppointmentId(pendingApt.getId());
                                    record.setRescheduleType("ADJACENT_ADVANCED");
                                    record.setIsAdjacentFieldAdvanced(true);
                                    record.setAdvancedAppointmentId(rainApt.getId());
                                    record.setOriginalStatus(AppointmentStatus.PENDING);
                                    record.setNewWorkDate(vacantDate);
                                    record.setNewStartTime(pendingApt.getStartTime());
                                    record.setNewEndTime(pendingApt.getEndTime());
                                    record.setReason("相邻地块空档，提前作业");
                                    record.setRescheduledBy(cooperativeId);
                                    rescheduleRecordRepository.save(record);
                                    
                                    advanced.add(pendingApt);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rescheduledCount", rescheduled.size());
        result.put("rescheduledAppointments", rescheduled);
        result.put("advancedCount", advanced.size());
        result.put("advancedAppointments", advanced);
        return result;
    }

    public List<Map<String, Object>> getMachineSlotUtilization(Long cooperativeId, LocalDate startDate, LocalDate endDate) {
        List<Machine> machines = machineRepository.findAll().stream()
                .filter(m -> m.getCooperativeId() != null && m.getCooperativeId().equals(cooperativeId))
                .toList();
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Machine machine : machines) {
            Map<String, Object> machineInfo = new HashMap<>();
            machineInfo.put("machineId", machine.getId());
            machineInfo.put("machineModel", machine.getMachineModel());
            machineInfo.put("plateNumber", machine.getPlateNumber());
            
            List<Map<String, Object>> dailySlots = new ArrayList<>();
            LocalDate date = startDate;
            int totalSlots = 0;
            int usedSlots = 0;
            int rainDelayedSlots = 0;
            
            while (!date.isAfter(endDate)) {
                List<Appointment> dayApts = appointmentRepository.findByMachineIdAndWorkDate(machine.getId(), date);
                int dayUsed = 0;
                int dayRainDelayed = 0;
                
                for (Appointment apt : dayApts) {
                    if (apt.getStatus() == AppointmentStatus.RAIN_DELAYED) {
                        dayRainDelayed++;
                    } else if (apt.getStatus() == AppointmentStatus.SCHEDULED || apt.getStatus() == AppointmentStatus.IN_PROGRESS
                            || apt.getStatus() == AppointmentStatus.COMPLETED) {
                        dayUsed++;
                    }
                }
                
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("date", date);
                dayInfo.put("usedSlots", dayUsed);
                dayInfo.put("rainDelayedSlots", dayRainDelayed);
                dayInfo.put("vacantSlots", Math.max(0, 4 - dayUsed - dayRainDelayed));
                dailySlots.add(dayInfo);
                
                totalSlots += 4;
                usedSlots += dayUsed;
                rainDelayedSlots += dayRainDelayed;
                
                date = date.plusDays(1);
            }
            
            machineInfo.put("dailySlots", dailySlots);
            machineInfo.put("totalSlots", totalSlots);
            machineInfo.put("usedSlots", usedSlots);
            machineInfo.put("rainDelayedSlots", rainDelayedSlots);
            machineInfo.put("vacantSlots", totalSlots - usedSlots - rainDelayedSlots);
            machineInfo.put("utilizationRate", totalSlots > 0 ? 
                    BigDecimal.valueOf(usedSlots).divide(BigDecimal.valueOf(totalSlots), 4, BigDecimal.ROUND_HALF_UP) 
                    : BigDecimal.ZERO);
            
            result.add(machineInfo);
        }
        
        return result;
    }
}
