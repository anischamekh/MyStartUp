package tn.iteam.backend.controller;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.iteam.backend.entity.EmployeeDocument;
import tn.iteam.backend.service.EmployeeDocumentService;

@RestController
@RequestMapping("/api/documents")
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    public EmployeeDocumentController(EmployeeDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<EmployeeDocument> list() {
        return documentService.findAllForCurrentUserOrHr();
    }

    @GetMapping("/user/{userId}")
    public List<EmployeeDocument> listForUser(@PathVariable Long userId) {
        return documentService.findForUser(userId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeDocument upload(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam("file") MultipartFile file
    ) {
        return documentService.upload(userId, name, type, file);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = documentService.download(id);
        String safe = resource.getFilename() != null ? resource.getFilename() : "document";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safe.replace("\"", "") + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok().build();
    }
}
