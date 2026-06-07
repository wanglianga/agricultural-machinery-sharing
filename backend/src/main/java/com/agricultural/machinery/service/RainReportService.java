package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.Appointment;
import com.agricultural.machinery.entity.Field;
import com.agricultural.machinery.entity.RainReport;
import com.agricultural.machinery.entity.RescheduleRecord;
import com.agricultural.machinery.enums.AppointmentStatus;
import com.agricultural.machinery.repository.AppointmentRepository;
import com.agricultural.machinery.repository.FieldRepository;
import com.agricultural.machinery.repository.RainReportRepository;
import com.agricultural.machinery.repository.RescheduleRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RainReportService {

    private final RainReportRepository rainReportRepository;
    private final FieldRepository fieldRepository;
    private final AppointmentRepository appointmentRepository;
    private final RescheduleRecordRepository rescheduleRecordRepository;

    public List<RainReport> getAllRainReports() {
        return rainReportRepository.findAll();
    }

    public RainReport getRainReportById(Long id) {
        return rainReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("雨后报告不存在"));
    }

    public List<RainReport> getRainReportsByFieldId(Long fieldId) {
        return rainReportRepository.findByFieldId(fieldId);
    }

    public List<RainReport> getRainReportsByAppointmentId(Long appointmentId) {
        return rainReportRepository.findByAppointmentId(appointmentId);
    }

    public List<RainReport> getRainReportsByOperatorId(Long operatorId) {
        return rainReportRepository.findByOperatorId(operatorId);
    }

    @Transactional
    public RainReport createRainReport(RainReport rainReport) {
        Field field = fieldRepository.findById(rainReport.getFieldId())
                .orElseThrow(() -> new RuntimeException("地块不存在"));

        Appointment appointment = appointmentRepository.findById(rainReport.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("预约不存在"));

        field.setRoadCondition(rainReport.getRoadCondition());
        field.setSoilMoisture(rainReport.getSoilMoisture());
        field.setLastRainReportTime(LocalDateTime.now());
        fieldRepository.save(field);

        RainReport saved = rainReportRepository.save(rainReport);

        if (Boolean.FALSE.equals(rainReport.getCanEnterField())) {
            handleRainDelayForAppointment(appointment, saved);
        }

        return saved;
    }

    private void handleRainDelayForAppointment(Appointment appointment, RainReport rainReport) {
        if (appointment.getStatus() == AppointmentStatus.SCHEDULED ||
            appointment.getStatus() == AppointmentStatus.PENDING) {

            RescheduleRecord record = new RescheduleRecord();
            record.setAppointmentId(appointment.getId());
            record.setRainReportId(rainReport.getId());
            record.setRescheduleType("RAIN_DELAY");
            record.setOriginalWorkDate(appointment.getWorkDate());
            record.setOriginalStartTime(appointment.getStartTime());
            record.setOriginalEndTime(appointment.getEndTime());
            record.setOriginalStatus(appointment.getStatus());
            record.setReason("雨后无法进田，路况: " + rainReport.getRoadCondition() + ", 土壤湿度: " + rainReport.getSoilMoisture());
            record.setRescheduledBy(rainReport.getOperatorId());
            rescheduleRecordRepository.save(record);

            appointment.setStatus(AppointmentStatus.RAIN_DELAYED);
            appointment.setRainDelayReason("雨后无法进田，路况: " + rainReport.getRoadCondition() + ", 土壤湿度: " + rainReport.getSoilMoisture());
            appointmentRepository.save(appointment);
        }
    }
}
