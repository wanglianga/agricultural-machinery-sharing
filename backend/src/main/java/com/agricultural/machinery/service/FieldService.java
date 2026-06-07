package com.agricultural.machinery.service;

import com.agricultural.machinery.entity.Field;
import com.agricultural.machinery.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FieldService {

    private final FieldRepository fieldRepository;

    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }

    public Field getFieldById(Long id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地块不存在"));
    }

    public List<Field> getFieldsByGrowerId(Long growerId) {
        return fieldRepository.findByGrowerId(growerId);
    }

    public List<Field> getFieldsByTown(String town) {
        return fieldRepository.findByTown(town);
    }

    @Transactional
    public Field createField(Field field) {
        return fieldRepository.save(field);
    }

    @Transactional
    public Field updateField(Long id, Field fieldDetails) {
        Field field = getFieldById(id);
        field.setFieldName(fieldDetails.getFieldName());
        field.setLocation(fieldDetails.getLocation());
        field.setVillage(fieldDetails.getVillage());
        field.setTown(fieldDetails.getTown());
        field.setArea(fieldDetails.getArea());
        field.setCropType(fieldDetails.getCropType());
        field.setSoilType(fieldDetails.getSoilType());
        field.setIrrigationMethod(fieldDetails.getIrrigationMethod());
        field.setContactName(fieldDetails.getContactName());
        field.setContactPhone(fieldDetails.getContactPhone());
        field.setRemark(fieldDetails.getRemark());
        return fieldRepository.save(field);
    }

    @Transactional
    public void deleteField(Long id) {
        fieldRepository.deleteById(id);
    }
}
