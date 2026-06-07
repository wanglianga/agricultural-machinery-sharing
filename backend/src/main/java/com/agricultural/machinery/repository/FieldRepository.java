package com.agricultural.machinery.repository;

import com.agricultural.machinery.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByGrowerId(Long growerId);
    List<Field> findByTown(String town);
    List<Field> findByVillage(String village);
}
