package com.agricultural.machinery.controller;

import com.agricultural.machinery.entity.RainReport;
import com.agricultural.machinery.service.RainReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rain-reports")
@RequiredArgsConstructor
public class RainReportController {

    private final RainReportService rainReportService;

    @GetMapping
    public ResponseEntity<List<RainReport>> getAllRainReports() {
        return ResponseEntity.ok(rainReportService.getAllRainReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RainReport> getRainReportById(@PathVariable Long id) {
        return ResponseEntity.ok(rainReportService.getRainReportById(id));
    }

    @GetMapping("/field/{fieldId}")
    public ResponseEntity<List<RainReport>> getRainReportsByFieldId(@PathVariable Long fieldId) {
        return ResponseEntity.ok(rainReportService.getRainReportsByFieldId(fieldId));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<RainReport>> getRainReportsByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(rainReportService.getRainReportsByAppointmentId(appointmentId));
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<RainReport>> getRainReportsByOperatorId(@PathVariable Long operatorId) {
        return ResponseEntity.ok(rainReportService.getRainReportsByOperatorId(operatorId));
    }

    @PostMapping
    public ResponseEntity<RainReport> createRainReport(@RequestBody RainReport rainReport) {
        return ResponseEntity.ok(rainReportService.createRainReport(rainReport));
    }
}
