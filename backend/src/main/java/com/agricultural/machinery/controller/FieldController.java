package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.Field;
import com.agricultural.machinery.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @GetMapping
    public ResponseEntity<List<Field>> getAllFields() {
        return ResponseEntity.ok(fieldService.getAllFields());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Field> getFieldById(@PathVariable Long id) {
        return ResponseEntity.ok(fieldService.getFieldById(id));
    }

    @GetMapping("/grower/{growerId}")
    public ResponseEntity<List<Field>> getFieldsByGrowerId(@PathVariable Long growerId) {
        return ResponseEntity.ok(fieldService.getFieldsByGrowerId(growerId));
    }

    @GetMapping("/town/{town}")
    public ResponseEntity<List<Field>> getFieldsByTown(@PathVariable String town) {
        return ResponseEntity.ok(fieldService.getFieldsByTown(town));
    }

    @PostMapping
    public ResponseEntity<Field> createField(@RequestBody Field field) {
        return ResponseEntity.ok(fieldService.createField(field));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Field> updateField(@PathVariable Long id, @RequestBody Field fieldDetails) {
        return ResponseEntity.ok(fieldService.updateField(id, fieldDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        fieldService.deleteField(id);
        return ResponseEntity.ok().build();
    }
}
