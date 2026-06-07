package com.agricultural.machinery.entity;

import com.agricultural.machinery.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String appointmentNo;

    @Column(nullable = false)
    private Long fieldId;

    @Column(nullable = false)
    private Long growerId;

    private Long cooperativeId;

    private Long machineId;

    private Long operatorId;

    @Column(nullable = false)
    private String workType;

    @Column(nullable = false)
    private LocalDate workDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    private LocalDate rainDelayToDate;

    private String rainDelayReason;

    private Boolean crossVillage;

    private String sourceVillage;

    private String targetVillage;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
