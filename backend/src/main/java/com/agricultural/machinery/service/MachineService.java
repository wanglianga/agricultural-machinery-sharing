package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.Machine;
import com.agricultural.machinery.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public Machine getMachineById(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机具不存在"));
    }

    public List<Machine> getMachinesByCooperativeId(Long cooperativeId) {
        return machineRepository.findByCooperativeId(cooperativeId);
    }

    public List<Machine> getAvailableMachines() {
        return machineRepository.findByAvailableTrue();
    }

    @Transactional
    public Machine createMachine(Machine machine) {
        if (machine.getAvailable() == null) {
            machine.setAvailable(true);
        }
        return machineRepository.save(machine);
    }

    @Transactional
    public Machine updateMachine(Long id, Machine machineDetails) {
        Machine machine = getMachineById(id);
        machine.setMachineModel(machineDetails.getMachineModel());
        machine.setMachineType(machineDetails.getMachineType());
        machine.setPlateNumber(machineDetails.getPlateNumber());
        machine.setHourMeter(machineDetails.getHourMeter());
        machine.setOperatorId(machineDetails.getOperatorId());
        machine.setOperatorName(machineDetails.getOperatorName());
        machine.setServiceArea(machineDetails.getServiceArea());
        machine.setPricePerHour(machineDetails.getPricePerHour());
        machine.setPricePerMu(machineDetails.getPricePerMu());
        machine.setWorkTypes(machineDetails.getWorkTypes());
        machine.setAvailable(machineDetails.getAvailable());
        machine.setRemark(machineDetails.getRemark());
        return machineRepository.save(machine);
    }

    @Transactional
    public void deleteMachine(Long id) {
        machineRepository.deleteById(id);
    }
}
