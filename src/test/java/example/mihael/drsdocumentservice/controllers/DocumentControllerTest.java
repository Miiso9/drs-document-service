package example.mihael.drsdocumentservice.controllers;

import example.mihael.drsdocumentservice.auth.SecurityConfig;
import example.mihael.drsdocumentservice.dto.EmailResponse;
import example.mihael.drsdocumentservice.models.Document;
import example.mihael.drsdocumentservice.services.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    private final UUID testUuid = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "admin")
    void should_save_document() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test content".getBytes()
        );

        Document mockDocument = new Document();
        mockDocument.setId(testUuid);
        mockDocument.setName("test.txt");
        mockDocument.setPath("S3");
        mockDocument.setObjectKey("test-key");

        when(documentService.saveDocument(Mockito.any()))
                .thenReturn(mockDocument);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/documents")
                        .file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUuid.toString()))
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.path").value("S3"));
    }

    @Test
    @WithMockUser(roles = "admin")
    void should_get_document() throws Exception {
        // Given
        Document mockDocument = new Document();
        mockDocument.setId(testUuid);
        mockDocument.setName("test.txt");
        mockDocument.setPath("S3");

        when(documentService.getDocument(testUuid))
                .thenReturn(mockDocument);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/documents/{id}", testUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUuid.toString()))
                .andExpect(jsonPath("$.name").value("test.txt"));
    }

    @Test
    @WithMockUser(roles = "admin")
    void should_send_document_in_email() throws Exception {
        // Given
        EmailResponse mockResponse = new EmailResponse(
                "Email sent successfully",
                "test.txt",
                "test@example.com"
        );

        when(documentService.sendDocumentToEmail(
                eq(testUuid),
                eq("test@example.com"),
                eq("Test Subject"),
                eq("Test Body")
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/documents/{id}/send", testUuid)
                        .with(csrf())
                        .param("recipientEmail", "test@example.com")
                        .param("mailSubject", "Test Subject")
                        .param("mailBody", "Test Body"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email sent successfully"))
                .andExpect(jsonPath("$.documentName").value("test.txt"))
                .andExpect(jsonPath("$.recipientEmail").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void should_forbidden_access_for_unauthorized_user() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/documents/{id}", testUuid))
                .andExpect(status().isForbidden());
    }
}
