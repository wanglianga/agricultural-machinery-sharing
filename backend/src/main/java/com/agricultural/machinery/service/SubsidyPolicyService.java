package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.SubsidyPolicy;
import com.agricultural.machinery.repository.SubsidyPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubsidyPolicyService {

    private final SubsidyPolicyRepository subsidyPolicyRepository;

    public List<SubsidyPolicy> getAllPolicies() {
        return subsidyPolicyRepository.findAll();
    }

    public List<SubsidyPolicy> getActivePolicies() {
        return subsidyPolicyRepository.findByActiveTrue();
    }

    public SubsidyPolicy getPolicyById(Long id) {
        return subsidyPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("补贴政策不存在"));
    }

    @Transactional
    public SubsidyPolicy createPolicy(SubsidyPolicy policy) {
        if (policy.getActive() == null) {
            policy.setActive(true);
        }
        return subsidyPolicyRepository.save(policy);
    }

    @Transactional
    public SubsidyPolicy updatePolicy(Long id, SubsidyPolicy policyDetails) {
        SubsidyPolicy policy = getPolicyById(id);
        policy.setPolicyName(policyDetails.getPolicyName());
        policy.setPolicyCode(policyDetails.getPolicyCode());
        policy.setWorkType(policyDetails.getWorkType());
        policy.setSubsidyPerMu(policyDetails.getSubsidyPerMu());
        policy.setSubsidyPerHour(policyDetails.getSubsidyPerHour());
        policy.setFuelSubsidyPerLiter(policyDetails.getFuelSubsidyPerLiter());
        policy.setSubsidyRatio(policyDetails.getSubsidyRatio());
        policy.setCrossVillageApplicable(policyDetails.getCrossVillageApplicable());
        policy.setCrossVillageExtraSubsidy(policyDetails.getCrossVillageExtraSubsidy());
        policy.setEffectiveDate(policyDetails.getEffectiveDate());
        policy.setExpiryDate(policyDetails.getExpiryDate());
        policy.setActive(policyDetails.getActive());
        policy.setApplicableRegion(policyDetails.getApplicableRegion());
        policy.setRemark(policyDetails.getRemark());
        return subsidyPolicyRepository.save(policy);
    }

    @Transactional
    public void deletePolicy(Long id) {
        subsidyPolicyRepository.deleteById(id);
    }
}
