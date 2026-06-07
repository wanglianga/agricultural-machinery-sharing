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
@Table(name = "fuel_tickets")
public class FuelTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticketNo;

    @Column(nullable = false)
    private Long workOrderId;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false)
    private Long machineId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fuelAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    private String gasStation;

    private String ticketImageUrl;

    private Boolean abnormal;

    private String abnormalReason;

    private Boolean verified;

    private Long verifiedBy;

    private LocalDateTime verifiedAt;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
