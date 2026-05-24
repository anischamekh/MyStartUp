package tn.iteam.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.dto.ReportsSummaryDto;
import tn.iteam.backend.service.ReportsService;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/summary")
    public ReportsSummaryDto summary() {
        return reportsService.summary();
    }

    @GetMapping("/export/summary.pdf")
    public ResponseEntity<byte[]> exportPdf() {
        byte[] data = reportsService.exportSummaryPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hrm-summary.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/export/summary.xlsx")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] data = reportsService.exportSummaryExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hrm-summary.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
