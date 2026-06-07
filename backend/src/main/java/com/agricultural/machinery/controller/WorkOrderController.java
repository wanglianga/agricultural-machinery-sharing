package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.WorkOrder;
import com.agricultural.machinery.service.WorkOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ResponseEntity<List<WorkOrder>> getAllWorkOrders() {
        return ResponseEntity.ok(workOrderService.getAllWorkOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.getWorkOrderById(id));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByOperatorId(@PathVariable Long operatorId) {
        return ResponseEntity.ok(workOrderService.getWorkOrdersByOperatorId(operatorId));
    }

    @GetMapping("/grower/{growerId}")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByGrowerId(@PathVariable Long growerId) {
        return ResponseEntity.ok(workOrderService.getWorkOrdersByGrowerId(growerId));
    }

    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByCooperativeId(@PathVariable Long cooperativeId) {
        return ResponseEntity.ok(workOrderService.getWorkOrdersByCooperativeId(cooperativeId));
    }

    @PostMapping("/from-appointment/{appointmentId}")
    public ResponseEntity<WorkOrder> createWorkOrderFromAppointment(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(workOrderService.createWorkOrderFromAppointment(appointmentId));
    }

    @PostMapping("/{id}/arrive")
    public ResponseEntity<WorkOrder> reportArrival(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.reportArrival(id));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<WorkOrder> startWork(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.startWork(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<WorkOrder> completeWork(
            @PathVariable Long id,
            @RequestBody CompleteWorkRequest request) {
        return ResponseEntity.ok(workOrderService.completeWork(
                id, request.getActualArea(), request.getCompletionPhotoUrl()));
    }

    @PostMapping("/{id}/confirm-area")
    public ResponseEntity<WorkOrder> growerConfirmArea(
            @PathVariable Long id,
            @RequestBody ConfirmAreaRequest request) {
        return ResponseEntity.ok(workOrderService.growerConfirmArea(
                id, request.getConfirmedArea(), request.getAreaConsistent(), request.getDisputeReason()));
    }

    @PostMapping("/{id}/resolve-dispute")
    public ResponseEntity<WorkOrder> resolveAreaDispute(
            @PathVariable Long id,
            @RequestBody ResolveDisputeRequest request) {
        return ResponseEntity.ok(workOrderService.resolveAreaDispute(
                id, request.getFinalArea(), request.getResolutionRemark()));
    }

    @Data
    public static class CompleteWorkRequest {
        private BigDecimal actualArea;
        private String completionPhotoUrl;
    }

    @Data
    public static class ConfirmAreaRequest {
        private BigDecimal confirmedArea;
        private Boolean areaConsistent;
        private String disputeReason;
    }

    @Data
    public static class ResolveDisputeRequest {
        private BigDecimal finalArea;
        private String resolutionRemark;
    }
}
