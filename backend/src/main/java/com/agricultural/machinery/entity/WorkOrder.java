package com.agricultural.machinery.entity;

import com.agricultural.machinery.enums.WorkOrderStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "work_orders")
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private Long appointmentId;

    @Column(nullable = false)
    private Long fieldId;

    @Column(nullable = false)
    private Long machineId;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false)
    private Long growerId;

    private Long cooperativeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;

    private LocalDateTime arriveTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal workHours;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualArea;

    @Column(precision = 10, scale = 2)
    private BigDecimal growerConfirmedArea;

    private Boolean areaConsistent;

    private String areaDisputeReason;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalSettledArea;

    private String completionPhotoUrl;

    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
