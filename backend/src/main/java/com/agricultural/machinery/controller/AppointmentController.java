package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.Appointment;
import com.agricultural.machinery.entity.RescheduleRecord;
import com.agricultural.machinery.service.AppointmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/grower/{growerId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByGrowerId(@PathVariable Long growerId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByGrowerId(growerId));
    }

    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<List<Appointment>> getAppointmentsByCooperativeId(@PathVariable Long cooperativeId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByCooperativeId(cooperativeId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDate(date));
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.createAppointment(appointment));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<Appointment> scheduleAppointment(
            @PathVariable Long id,
            @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(appointmentService.scheduleAppointment(
                id, request.getCooperativeId(), request.getMachineId(), request.getOperatorId()));
    }

    @PostMapping("/{id}/rain-delay")
    public ResponseEntity<Appointment> handleRainDelay(
            @PathVariable Long id,
            @RequestBody RainDelayRequest request) {
        return ResponseEntity.ok(appointmentService.handleRainDelay(
                id, request.getNewDate(), request.getReason()));
    }

    @PostMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> rescheduleAfterRain(
            @PathVariable Long id,
            @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleAfterRain(
                id, request.getNewDate(), request.getStartTime(), request.getEndTime()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, reason));
    }

    @GetMapping("/{id}/reschedule-records")
    public ResponseEntity<List<RescheduleRecord>> getRescheduleRecordsByAppointmentId(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getRescheduleRecordsByAppointmentId(id));
    }

    @GetMapping("/grower/{growerId}/reschedule-records")
    public ResponseEntity<List<RescheduleRecord>> getRescheduleRecordsByGrowerId(@PathVariable Long growerId) {
        return ResponseEntity.ok(appointmentService.getRescheduleRecordsByGrowerId(growerId));
    }

    @PostMapping("/{id}/reschedule-v2")
    public ResponseEntity<Appointment> rescheduleAfterRainV2(
            @PathVariable Long id,
            @RequestBody RescheduleV2Request request) {
        return ResponseEntity.ok(appointmentService.rescheduleAfterRain(
                id, request.getNewDate(), request.getStartTime(), request.getEndTime(), request.getRescheduledBy()));
    }

    @PostMapping("/batch-reschedule-rain")
    public ResponseEntity<Map<String, Object>> batchRescheduleRainDelayed(
            @RequestBody BatchRescheduleRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleRainDelayedAppointments(
                request.getCooperativeId(), request.getFromDate()));
    }

    @GetMapping("/machine-slot-utilization")
    public ResponseEntity<List<Map<String, Object>>> getMachineSlotUtilization(
            @RequestParam Long cooperativeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ResponseEntity.ok(appointmentService.getMachineSlotUtilization(cooperativeId, startDate, endDate));
    }

    @Data
    public static class ScheduleRequest {
        private Long cooperativeId;
        private Long machineId;
        private Long operatorId;
    }

    @Data
    public static class RainDelayRequest {
        private LocalDate newDate;
        private String reason;
    }

    @Data
    public static class RescheduleRequest {
        private LocalDate newDate;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data
    public static class RescheduleV2Request {
        private LocalDate newDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Long rescheduledBy;
    }

    @Data
    public static class BatchRescheduleRequest {
        private Long cooperativeId;
        private LocalDate fromDate;
    }
}
