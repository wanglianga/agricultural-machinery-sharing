package com.agricultural.machinery.entity;

import com.agricultural.machinery.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "settlements")
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String settlementNo;

    @Column(nullable = false)
    private Long workOrderId;

    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long growerId;

    @Column(nullable = false)
    private Long cooperativeId;

    private Long operatorId;

    @Column(precision = 10, scale = 2)
    private BigDecimal settledArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal workHours;

    @Column(precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal fuelCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal fuelSubsidy;

    @Column(precision = 10, scale = 2)
    private BigDecimal operationSubsidy;

    @Column(precision = 10, scale = 2)
    private BigDecimal crossVillageSubsidy;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalSubsidy;

    @Column(precision = 10, scale = 2)
    private BigDecimal growerPayable;

    @Column(precision = 10, scale = 2)
    private BigDecimal cooperativeReceivable;

    @Column(precision = 10, scale = 2)
    private BigDecimal operatorReceivable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    private Long approvedBy;

    private LocalDateTime approvedAt;

    private String approvalRemark;

    private String paymentVoucher;

    private LocalDateTime paidAt;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
