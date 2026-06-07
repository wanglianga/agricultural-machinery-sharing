package com.agricultural.machinery.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "machines")
public class Machine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String machineModel;

    @Column(nullable = false)
    private String machineType;

    private String plateNumber;

    @Column(nullable = false)
    private Integer hourMeter;

    @Column(nullable = false)
    private Long cooperativeId;

    private Long operatorId;

    private String operatorName;

    private String serviceArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerMu;

    private String workTypes;

    private Boolean available;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
