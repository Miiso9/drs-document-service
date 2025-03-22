package example.mihael.drsdocumentservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    private final String senderEmail = "test@example.com";
    private final String sendGridApiKey = "dummy-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "senderEmail", senderEmail);
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", sendGridApiKey);
    }

    @Test
    void should_send_email_with_attachment() throws Exception {
        // Given
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        byte[] attachmentData = "test data".getBytes();
        String attachmentName = "test.txt";

        final List<Object[]> capturedArgs = new ArrayList<>();
        try (MockedConstruction<SendGrid> mockedConstruction = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    capturedArgs.add(context.arguments().toArray());
                    Response mockResponse = new Response();
                    mockResponse.setStatusCode(202);
                    mockResponse.setBody("Success");
                    mockResponse.setHeaders(new HashMap<>());
                    when(mock.api(any(Request.class))).thenReturn(mockResponse);
                })) {

            // When
            emailService.sendEmailWithAttachment(to, subject, body, attachmentData, attachmentName);

            // Then
            assertEquals(1, capturedArgs.size());
            assertEquals(sendGridApiKey, capturedArgs.get(0)[0]);

            SendGrid sendGridMock = mockedConstruction.constructed().get(0);
            ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            verify(sendGridMock).api(requestCaptor.capture());

            Request capturedRequest = requestCaptor.getValue();
            assertEquals(Method.POST, capturedRequest.getMethod());
            assertEquals("mail/send", capturedRequest.getEndpoint());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(capturedRequest.getBody());

            JsonNode personalizations = root.path("personalizations");
            assertTrue(personalizations.isArray(), "'personalizations' should be an array");
            assertEquals(1, personalizations.size(), "'personalizations' must have 1 entry");

            JsonNode toEmails = personalizations.get(0).path("to");
            assertEquals(1, toEmails.size(), "Expected 1 recipient in 'to'");
            assertEquals(to, toEmails.get(0).path("email").asText());

            assertEquals(senderEmail, root.path("from").path("email").asText());
            assertEquals(to, root.path("personalizations").get(0).path("to").get(0).path("email").asText());
            assertEquals(subject, root.path("subject").asText());
            assertEquals(body, root.path("content").get(0).path("value").asText());
            assertEquals(attachmentName, root.path("attachments").get(0).path("filename").asText());
            assertEquals(
                    Base64.getEncoder().encodeToString(attachmentData),
                    root.path("attachments").get(0).path("content").asText()
            );
        }
    }

    @Test
    void should_throw_exception_on_sending_mail() {
        // Given
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        byte[] attachmentData = "test data".getBytes();
        String attachmentName = "test.txt";

        // When & Then
        try (MockedConstruction<SendGrid> ignored = mockConstruction(SendGrid.class,
                (mock, context) -> {
                    when(mock.api(any(Request.class))).thenThrow(new IOException("API Error"));
                })) {

            assertThrows(IOException.class, () ->
                    emailService.sendEmailWithAttachment(to, subject, body, attachmentData, attachmentName)
            );
        }
    }
}