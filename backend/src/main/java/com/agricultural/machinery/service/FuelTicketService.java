package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.FuelTicket;
import com.agricultural.machinery.repository.FuelTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuelTicketService {

    private final FuelTicketRepository fuelTicketRepository;

    public List<FuelTicket> getAllFuelTickets() {
        return fuelTicketRepository.findAll();
    }

    public FuelTicket getFuelTicketById(Long id) {
        return fuelTicketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("油票不存在"));
    }

    public List<FuelTicket> getFuelTicketsByWorkOrderId(Long workOrderId) {
        return fuelTicketRepository.findByWorkOrderId(workOrderId);
    }

    public List<FuelTicket> getFuelTicketsByOperatorId(Long operatorId) {
        return fuelTicketRepository.findByOperatorId(operatorId);
    }

    public List<FuelTicket> getAbnormalFuelTickets() {
        return fuelTicketRepository.findByAbnormalTrue();
    }

    public List<FuelTicket> getUnverifiedFuelTickets() {
        return fuelTicketRepository.findByVerifiedFalse();
    }

    @Transactional
    public FuelTicket createFuelTicket(FuelTicket fuelTicket) {
        if (fuelTicket.getTotalAmount() == null && fuelTicket.getFuelAmount() != null && fuelTicket.getUnitPrice() != null) {
            fuelTicket.setTotalAmount(fuelTicket.getFuelAmount().multiply(fuelTicket.getUnitPrice()));
        }
        if (fuelTicket.getAbnormal() == null) {
            fuelTicket.setAbnormal(false);
        }
        if (fuelTicket.getVerified() == null) {
            fuelTicket.setVerified(false);
        }
        detectAbnormality(fuelTicket);
        return fuelTicketRepository.save(fuelTicket);
    }

    @Transactional
    public FuelTicket verifyFuelTicket(Long id, Long verifierId, Boolean isNormal, String abnormalReason) {
        FuelTicket fuelTicket = getFuelTicketById(id);
        fuelTicket.setVerified(true);
        fuelTicket.setVerifiedBy(verifierId);
        fuelTicket.setVerifiedAt(LocalDateTime.now());
        if (!isNormal) {
            fuelTicket.setAbnormal(true);
            fuelTicket.setAbnormalReason(abnormalReason);
        } else {
            fuelTicket.setAbnormal(false);
            fuelTicket.setAbnormalReason(null);
        }
        return fuelTicketRepository.save(fuelTicket);
    }

    private void detectAbnormality(FuelTicket fuelTicket) {
        BigDecimal expectedMax = new BigDecimal("200");
        if (fuelTicket.getFuelAmount() != null && fuelTicket.getFuelAmount().compareTo(expectedMax) > 0) {
            fuelTicket.setAbnormal(true);
            fuelTicket.setAbnormalReason("加油量异常，超过单次最大限值");
        }
        if (fuelTicket.getUnitPrice() != null) {
            BigDecimal minPrice = new BigDecimal("5.0");
            BigDecimal maxPrice = new BigDecimal("10.0");
            if (fuelTicket.getUnitPrice().compareTo(minPrice) < 0 || fuelTicket.getUnitPrice().compareTo(maxPrice) > 0) {
                fuelTicket.setAbnormal(true);
                fuelTicket.setAbnormalReason("油价异常，不在合理范围内");
            }
        }
    }
}
