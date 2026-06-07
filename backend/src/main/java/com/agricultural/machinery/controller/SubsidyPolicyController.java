package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.SubsidyPolicy;
import com.agricultural.machinery.service.SubsidyPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subsidy-policies")
@RequiredArgsConstructor
public class SubsidyPolicyController {

    private final SubsidyPolicyService subsidyPolicyService;

    @GetMapping
    public ResponseEntity<List<SubsidyPolicy>> getAllPolicies() {
        return ResponseEntity.ok(subsidyPolicyService.getAllPolicies());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SubsidyPolicy>> getActivePolicies() {
        return ResponseEntity.ok(subsidyPolicyService.getActivePolicies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubsidyPolicy> getPolicyById(@PathVariable Long id) {
        return ResponseEntity.ok(subsidyPolicyService.getPolicyById(id));
    }

    @PostMapping
    public ResponseEntity<SubsidyPolicy> createPolicy(@RequestBody SubsidyPolicy policy) {
        return ResponseEntity.ok(subsidyPolicyService.createPolicy(policy));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubsidyPolicy> updatePolicy(@PathVariable Long id, @RequestBody SubsidyPolicy policyDetails) {
        return ResponseEntity.ok(subsidyPolicyService.updatePolicy(id, policyDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        subsidyPolicyService.deletePolicy(id);
        return ResponseEntity.ok().build();
    }
}
