package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.FuelTicket;
import com.agricultural.machinery.service.FuelTicketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fuel-tickets")
@RequiredArgsConstructor
public class FuelTicketController {

    private final FuelTicketService fuelTicketService;

    @GetMapping
    public ResponseEntity<List<FuelTicket>> getAllFuelTickets() {
        return ResponseEntity.ok(fuelTicketService.getAllFuelTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuelTicket> getFuelTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(fuelTicketService.getFuelTicketById(id));
    }

    @GetMapping("/work-order/{workOrderId}")
    public ResponseEntity<List<FuelTicket>> getFuelTicketsByWorkOrderId(@PathVariable Long workOrderId) {
        return ResponseEntity.ok(fuelTicketService.getFuelTicketsByWorkOrderId(workOrderId));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<FuelTicket>> getFuelTicketsByOperatorId(@PathVariable Long operatorId) {
        return ResponseEntity.ok(fuelTicketService.getFuelTicketsByOperatorId(operatorId));
    }

    @GetMapping("/abnormal")
    public ResponseEntity<List<FuelTicket>> getAbnormalFuelTickets() {
        return ResponseEntity.ok(fuelTicketService.getAbnormalFuelTickets());
    }

    @GetMapping("/unverified")
    public ResponseEntity<List<FuelTicket>> getUnverifiedFuelTickets() {
        return ResponseEntity.ok(fuelTicketService.getUnverifiedFuelTickets());
    }

    @PostMapping
    public ResponseEntity<FuelTicket> createFuelTicket(@RequestBody FuelTicket fuelTicket) {
        return ResponseEntity.ok(fuelTicketService.createFuelTicket(fuelTicket));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<FuelTicket> verifyFuelTicket(
            @PathVariable Long id,
            @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(fuelTicketService.verifyFuelTicket(
                id, request.getVerifierId(), request.getIsNormal(), request.getAbnormalReason()));
    }

    @Data
    public static class VerifyRequest {
        private Long verifierId;
        private Boolean isNormal;
        private String abnormalReason;
    }
}
