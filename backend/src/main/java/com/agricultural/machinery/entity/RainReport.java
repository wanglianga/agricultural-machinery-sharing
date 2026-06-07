package com.agricultural.machinery.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rain_reports")
public class RainReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fieldId;

    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long operatorId;

    private Long machineId;

    @Column(nullable = false)
    private String roadCondition;

    @Column(nullable = false)
    private String soilMoisture;

    private Integer rainfallMm;

    private Boolean canEnterField;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
