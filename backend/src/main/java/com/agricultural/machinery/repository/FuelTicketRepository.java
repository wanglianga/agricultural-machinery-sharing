package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.FuelTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelTicketRepository extends JpaRepository<FuelTicket, Long> {
    List<FuelTicket> findByWorkOrderId(Long workOrderId);
    List<FuelTicket> findByOperatorId(Long operatorId);
    List<FuelTicket> findByAbnormalTrue();
    List<FuelTicket> findByVerifiedFalse();
}
