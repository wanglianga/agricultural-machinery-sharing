package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.Settlement;
import com.agricultural.machinery.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByWorkOrderId(Long workOrderId);
    List<Settlement> findByGrowerId(Long growerId);
    List<Settlement> findByCooperativeId(Long cooperativeId);
    List<Settlement> findByStatus(SettlementStatus status);
}
