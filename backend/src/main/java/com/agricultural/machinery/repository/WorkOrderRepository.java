package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.WorkOrder;
import com.agricultural.machinery.enums.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByAppointmentId(Long appointmentId);
    List<WorkOrder> findByOperatorId(Long operatorId);
    List<WorkOrder> findByGrowerId(Long growerId);
    List<WorkOrder> findByCooperativeId(Long cooperativeId);
    List<WorkOrder> findByStatus(WorkOrderStatus status);
    List<WorkOrder> findByMachineId(Long machineId);
}
