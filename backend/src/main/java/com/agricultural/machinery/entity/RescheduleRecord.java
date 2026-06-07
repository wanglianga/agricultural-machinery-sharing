package com.agricultural.machinery.entity;

import com.agricultural.machinery.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "reschedule_records")
public class RescheduleRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long appointmentId;

    private Long rainReportId;

    @Column(nullable = false)
    private String rescheduleType;

    private LocalDate originalWorkDate;

    private LocalTime originalStartTime;

    private LocalTime originalEndTime;

    private LocalDate newWorkDate;

    private LocalTime newStartTime;

    private LocalTime newEndTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus originalStatus;

    private String reason;

    private Long rescheduledBy;

    private Boolean isAdjacentFieldAdvanced;

    private Long advancedAppointmentId;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
