package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByCooperativeId(Long cooperativeId);
    List<Machine> findByAvailableTrue();
    List<Machine> findByMachineType(String machineType);
}
