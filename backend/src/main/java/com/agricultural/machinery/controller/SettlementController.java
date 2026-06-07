package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.Settlement;
import com.agricultural.machinery.service.SettlementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<List<Settlement>> getAllSettlements() {
        return ResponseEntity.ok(settlementService.getAllSettlements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Settlement> getSettlementById(@PathVariable Long id) {
        return ResponseEntity.ok(settlementService.getSettlementById(id));
    }

    @GetMapping("/grower/{growerId}")
    public ResponseEntity<List<Settlement>> getSettlementsByGrowerId(@PathVariable Long growerId) {
        return ResponseEntity.ok(settlementService.getSettlementsByGrowerId(growerId));
    }

    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<List<Settlement>> getSettlementsByCooperativeId(@PathVariable Long cooperativeId) {
        return ResponseEntity.ok(settlementService.getSettlementsByCooperativeId(cooperativeId));
    }

    @PostMapping("/calculate/{workOrderId}")
    public ResponseEntity<Settlement> calculateSettlement(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(settlementService.calculateSettlement(workOrderId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Settlement> approveSettlement(
            @PathVariable Long id,
            @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(settlementService.approveSettlement(
                id, request.getApproverId(), request.getRemark()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Settlement> rejectSettlement(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        return ResponseEntity.ok(settlementService.rejectSettlement(id, reason));
    }

    @PostMapping("/{id}/paid")
    public ResponseEntity<Settlement> markAsPaid(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String paymentVoucher = body.get("paymentVoucher");
        return ResponseEntity.ok(settlementService.markAsPaid(id, paymentVoucher));
    }

    @Data
    public static class ApproveRequest {
        private Long approverId;
        private String remark;
    }
}
