package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.Appointment;
import com.agricultural.machinery.entity.WorkOrder;
import com.agricultural.machinery.enums.AppointmentStatus;
import com.agricultural.machinery.enums.WorkOrderStatus;
import com.agricultural.machinery.repository.AppointmentRepository;
import com.agricultural.machinery.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final AppointmentRepository appointmentRepository;

    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findAll();
    }

    public WorkOrder getWorkOrderById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("作业单不存在"));
    }

    public List<WorkOrder> getWorkOrdersByOperatorId(Long operatorId) {
        return workOrderRepository.findByOperatorId(operatorId);
    }

    public List<WorkOrder> getWorkOrdersByGrowerId(Long growerId) {
        return workOrderRepository.findByGrowerId(growerId);
    }

    public List<WorkOrder> getWorkOrdersByCooperativeId(Long cooperativeId) {
        return workOrderRepository.findByCooperativeId(cooperativeId);
    }

    @Transactional
    public WorkOrder createWorkOrderFromAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new RuntimeException("只有已排程的预约才能创建作业单");
        }

        WorkOrder workOrder = new WorkOrder();
        workOrder.setOrderNo("WO" + System.currentTimeMillis());
        workOrder.setAppointmentId(appointmentId);
        workOrder.setFieldId(appointment.getFieldId());
        workOrder.setMachineId(appointment.getMachineId());
        workOrder.setOperatorId(appointment.getOperatorId());
        workOrder.setGrowerId(appointment.getGrowerId());
        workOrder.setCooperativeId(appointment.getCooperativeId());
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointmentRepository.save(appointment);

        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder reportArrival(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        workOrder.setStatus(WorkOrderStatus.ARRIVED);
        workOrder.setArriveTime(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder startWork(Long id) {
        WorkOrder workOrder = getWorkOrderById(id);
        workOrder.setStatus(WorkOrderStatus.WORKING);
        workOrder.setStartTime(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder completeWork(Long id, BigDecimal actualArea, String completionPhotoUrl) {
        WorkOrder workOrder = getWorkOrderById(id);
        workOrder.setStatus(WorkOrderStatus.AWAITING_CONFIRMATION);
        workOrder.setEndTime(LocalDateTime.now());
        workOrder.setActualArea(actualArea);
        workOrder.setCompletionPhotoUrl(completionPhotoUrl);

        if (workOrder.getStartTime() != null) {
            Duration duration = Duration.between(workOrder.getStartTime(), workOrder.getEndTime());
            BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
            workOrder.setWorkHours(hours);
        }

        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder growerConfirmArea(Long id, BigDecimal confirmedArea, Boolean areaConsistent, String disputeReason) {
        WorkOrder workOrder = getWorkOrderById(id);
        workOrder.setGrowerConfirmedArea(confirmedArea);
        workOrder.setAreaConsistent(areaConsistent);

        if (areaConsistent) {
            workOrder.setStatus(WorkOrderStatus.CONFIRMED);
            workOrder.setFinalSettledArea(confirmedArea);
        } else {
            workOrder.setStatus(WorkOrderStatus.DISPUTED);
            workOrder.setAreaDisputeReason(disputeReason);
            
            Appointment appointment = appointmentRepository.findById(workOrder.getAppointmentId()).orElse(null);
            if (appointment != null) {
                appointment.setStatus(AppointmentStatus.AREA_DISPUTE);
                appointmentRepository.save(appointment);
            }
        }

        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder resolveAreaDispute(Long id, BigDecimal finalArea, String resolutionRemark) {
        WorkOrder workOrder = getWorkOrderById(id);
        workOrder.setStatus(WorkOrderStatus.CONFIRMED);
        workOrder.setFinalSettledArea(finalArea);
        workOrder.setRemark(resolutionRemark);

        Appointment appointment = appointmentRepository.findById(workOrder.getAppointmentId()).orElse(null);
        if (appointment != null) {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
            appointmentRepository.save(appointment);
        }

        return workOrderRepository.save(workOrder);
    }
}
