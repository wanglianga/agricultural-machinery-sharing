package com.agricultural.machinery.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "subsidy_policies")
public class SubsidyPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String policyName;

    @Column(nullable = false)
    private String policyCode;

    private String workType;

    @Column(precision = 10, scale = 2)
    private BigDecimal subsidyPerMu;

    @Column(precision = 10, scale = 2)
    private BigDecimal subsidyPerHour;

    @Column(precision = 10, scale = 2)
    private BigDecimal fuelSubsidyPerLiter;

    @Column(precision = 5, scale = 2)
    private BigDecimal subsidyRatio;

    private Boolean crossVillageApplicable;

    private BigDecimal crossVillageExtraSubsidy;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private Boolean active;

    private String applicableRegion;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
