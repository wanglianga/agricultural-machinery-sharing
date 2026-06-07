package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.SubsidyPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubsidyPolicyRepository extends JpaRepository<SubsidyPolicy, Long> {
    List<SubsidyPolicy> findByActiveTrue();
    List<SubsidyPolicy> findByWorkType(String workType);
}
