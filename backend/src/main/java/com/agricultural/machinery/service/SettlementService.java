package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.*;
import com.agricultural.machinery.enums.SettlementStatus;
import com.agricultural.machinery.enums.WorkOrderStatus;
import com.agricultural.machinery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AppointmentRepository appointmentRepository;
    private final MachineRepository machineRepository;
    private final SubsidyPolicyRepository subsidyPolicyRepository;
    private final FuelTicketRepository fuelTicketRepository;

    public List<Settlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    public Settlement getSettlementById(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("结算单不存在"));
    }

    public List<Settlement> getSettlementsByGrowerId(Long growerId) {
        return settlementRepository.findByGrowerId(growerId);
    }

    public List<Settlement> getSettlementsByCooperativeId(Long cooperativeId) {
        return settlementRepository.findByCooperativeId(cooperativeId);
    }

    @Transactional
    public Settlement calculateSettlement(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException("作业单不存在"));

        if (workOrder.getStatus() != WorkOrderStatus.CONFIRMED) {
            throw new RuntimeException("只有已确认的作业单才能结算");
        }

        Appointment appointment = appointmentRepository.findById(workOrder.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("预约不存在"));

        Machine machine = machineRepository.findById(workOrder.getMachineId())
                .orElseThrow(() -> new RuntimeException("机具不存在"));

        Settlement settlement = new Settlement();
        settlement.setSettlementNo("SET" + System.currentTimeMillis());
        settlement.setWorkOrderId(workOrderId);
        settlement.setAppointmentId(workOrder.getAppointmentId());
        settlement.setGrowerId(workOrder.getGrowerId());
        settlement.setCooperativeId(workOrder.getCooperativeId());
        settlement.setOperatorId(workOrder.getOperatorId());
        settlement.setSettledArea(workOrder.getFinalSettledArea());
        settlement.setWorkHours(workOrder.getWorkHours());

        BigDecimal serviceFee = BigDecimal.ZERO;
        if (machine.getPricePerMu() != null && workOrder.getFinalSettledArea() != null) {
            serviceFee = machine.getPricePerMu().multiply(workOrder.getFinalSettledArea());
        }
        settlement.setServiceFee(serviceFee);

        List<FuelTicket> fuelTickets = fuelTicketRepository.findByWorkOrderId(workOrderId);
        BigDecimal totalFuelCost = fuelTickets.stream()
                .filter(ft -> !ft.getAbnormal() && ft.getVerified())
                .map(FuelTicket::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFuelAmount = fuelTickets.stream()
                .filter(ft -> !ft.getAbnormal() && ft.getVerified())
                .map(FuelTicket::getFuelAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        settlement.setFuelCost(totalFuelCost);

        List<SubsidyPolicy> activePolicies = subsidyPolicyRepository.findByActiveTrue();
        
        BigDecimal fuelSubsidy = BigDecimal.ZERO;
        BigDecimal operationSubsidy = BigDecimal.ZERO;
        BigDecimal crossVillageSubsidy = BigDecimal.ZERO;

        for (SubsidyPolicy policy : activePolicies) {
            if (policy.getFuelSubsidyPerLiter() != null) {
                fuelSubsidy = fuelSubsidy.add(policy.getFuelSubsidyPerLiter().multiply(totalFuelAmount));
            }
            if (policy.getSubsidyPerMu() != null && workOrder.getFinalSettledArea() != null) {
                operationSubsidy = operationSubsidy.add(policy.getSubsidyPerMu().multiply(workOrder.getFinalSettledArea()));
            }
            if (policy.getCrossVillageApplicable() != null && policy.getCrossVillageApplicable()
                    && appointment.getCrossVillage() != null && appointment.getCrossVillage()
                    && policy.getCrossVillageExtraSubsidy() != null) {
                crossVillageSubsidy = crossVillageSubsidy.add(policy.getCrossVillageExtraSubsidy());
            }
        }

        settlement.setFuelSubsidy(fuelSubsidy);
        settlement.setOperationSubsidy(operationSubsidy);
        settlement.setCrossVillageSubsidy(crossVillageSubsidy);
        settlement.setTotalSubsidy(fuelSubsidy.add(operationSubsidy).add(crossVillageSubsidy));

        BigDecimal growerPayable = serviceFee.subtract(settlement.getTotalSubsidy());
        if (growerPayable.compareTo(BigDecimal.ZERO) < 0) {
            growerPayable = BigDecimal.ZERO;
        }
        settlement.setGrowerPayable(growerPayable);

        BigDecimal cooperativeReceivable = serviceFee.multiply(new BigDecimal("0.7"));
        BigDecimal operatorReceivable = serviceFee.multiply(new BigDecimal("0.3"));
        settlement.setCooperativeReceivable(cooperativeReceivable);
        settlement.setOperatorReceivable(operatorReceivable);

        settlement.setStatus(SettlementStatus.CALCULATED);

        return settlementRepository.save(settlement);
    }

    @Transactional
    public Settlement approveSettlement(Long id, Long approverId, String remark) {
        Settlement settlement = getSettlementById(id);
        settlement.setStatus(SettlementStatus.APPROVED);
        settlement.setApprovedBy(approverId);
        settlement.setApprovedAt(LocalDateTime.now());
        settlement.setApprovalRemark(remark);
        return settlementRepository.save(settlement);
    }

    @Transactional
    public Settlement rejectSettlement(Long id, String reason) {
        Settlement settlement = getSettlementById(id);
        settlement.setStatus(SettlementStatus.REJECTED);
        settlement.setApprovalRemark(reason);
        return settlementRepository.save(settlement);
    }

    @Transactional
    public Settlement markAsPaid(Long id, String paymentVoucher) {
        Settlement settlement = getSettlementById(id);
        settlement.setStatus(SettlementStatus.PAID);
        settlement.setPaidAt(LocalDateTime.now());
        settlement.setPaymentVoucher(paymentVoucher);
        return settlementRepository.save(settlement);
    }
}
