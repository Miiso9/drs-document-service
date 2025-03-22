package example.mihael.drsdocumentservice.services;

import example.mihael.drsdocumentservice.dto.EmailResponse;
import example.mihael.drsdocumentservice.exceptions.ResourceNotFoundException;
import example.mihael.drsdocumentservice.models.Document;
import example.mihael.drsdocumentservice.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void saveDocument() throws IOException {

        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getBytes()).thenReturn("test content".getBytes());

        Document document = new Document();
        document.setName("test.txt");
        document.setPath("S3");
        document.setContentType("text/plain");
        document.setObjectKey("objectKey");

        // When
        when(s3Service.uploadFileToS3(any(byte[].class), anyString())).thenReturn("objectKey");
        when(documentRepository.save(any(Document.class))).thenReturn(document);
        Document savedDocument = documentService.saveDocument(file);

        // Then
        assertNotNull(savedDocument);
        assertEquals("test.txt", savedDocument.getName());
        assertEquals("S3", savedDocument.getPath());
        assertEquals("text/plain", savedDocument.getContentType());
        assertEquals("objectKey", savedDocument.getObjectKey());
        verify(s3Service, times(1)).uploadFileToS3(any(byte[].class), anyString());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void getDocument() {
        // Given
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setName("test.txt");

        // When
        when(documentRepository.findById(id)).thenReturn(Optional.of(document));
        Document foundDocument = documentService.getDocument(id);

        // Then
        assertNotNull(foundDocument);
        assertEquals(id, foundDocument.getId());
        assertEquals("test.txt", foundDocument.getName());
        verify(documentRepository, times(1)).findById(id);
    }

    @Test
    void getDocument_NotFound() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        // Then
        assertThrows(ResourceNotFoundException.class, () -> documentService.getDocument(id));
        verify(documentRepository, times(1)).findById(id);
    }

    @Test
    void sendDocumentToEmail() throws IOException {
        // Given
        UUID id = UUID.randomUUID();
        Document document = new Document();
        document.setId(id);
        document.setName("test.txt");
        document.setObjectKey("objectKey");

        // When
        when(documentRepository.findById(id)).thenReturn(Optional.of(document));
        when(s3Service.downloadFileFromS3("objectKey")).thenReturn("test content".getBytes());
        EmailResponse response = documentService.sendDocumentToEmail(id, "test@example.com",
                "Subject", "Body");

        // Then
        assertNotNull(response);
        assertEquals("Email sent successfully", response.getMessage());
        assertEquals("test.txt", response.getDocumentName());
        assertEquals("test@example.com", response.getRecipientEmail());
        verify(documentRepository, times(1)).findById(id);
        verify(s3Service, times(1)).downloadFileFromS3("objectKey");
        verify(emailService, times(1)).sendEmailWithAttachment(eq("test@example.com"),
                eq("Subject"), eq("Body"), any(byte[].class), eq("test.txt"));
    }
}