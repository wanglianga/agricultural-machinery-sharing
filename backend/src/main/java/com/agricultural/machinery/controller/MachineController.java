package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.Machine;
import com.agricultural.machinery.service.MachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;

    @GetMapping
    public ResponseEntity<List<Machine>> getAllMachines() {
        return ResponseEntity.ok(machineService.getAllMachines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Machine> getMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(machineService.getMachineById(id));
    }

    @GetMapping("/cooperative/{cooperativeId}")
    public ResponseEntity<List<Machine>> getMachinesByCooperativeId(@PathVariable Long cooperativeId) {
        return ResponseEntity.ok(machineService.getMachinesByCooperativeId(cooperativeId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Machine>> getAvailableMachines() {
        return ResponseEntity.ok(machineService.getAvailableMachines());
    }

    @PostMapping
    public ResponseEntity<Machine> createMachine(@RequestBody Machine machine) {
        return ResponseEntity.ok(machineService.createMachine(machine));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Machine> updateMachine(@PathVariable Long id, @RequestBody Machine machineDetails) {
        return ResponseEntity.ok(machineService.updateMachine(id, machineDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.ok().build();
    }
}
