package example.mihael.drsdocumentservice.controllers;

import example.mihael.drsdocumentservice.dto.EmailResponse;
import example.mihael.drsdocumentservice.models.Document;
import example.mihael.drsdocumentservice.services.DocumentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    DocumentService documentService;

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> saveDocument(
            @Parameter(description = "File to be uploaded", content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(documentService.saveDocument(file));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_admin')")
    public ResponseEntity<Document> getDocument(
            @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocument(id));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ROLE_admin')")
    public ResponseEntity<EmailResponse> sendDocumentToEmail(
            @PathVariable UUID id,
            @RequestParam String recipientEmail,
            @RequestParam String mailSubject,
            @RequestParam String mailBody) throws IOException {
        return ResponseEntity.ok(documentService.sendDocumentToEmail(id, recipientEmail, mailSubject, mailBody));
    }
}
