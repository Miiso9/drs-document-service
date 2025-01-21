package example.mihael.drsdocumentservice.services;

import example.mihael.drsdocumentservice.dto.EmailResponse;
import example.mihael.drsdocumentservice.exceptions.ResourceNotFoundException;
import example.mihael.drsdocumentservice.models.Document;
import example.mihael.drsdocumentservice.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private EmailService emailService;

    public Document saveDocument(MultipartFile file) throws IOException {
        String fileName= file.getOriginalFilename();

        Document document = new Document();
        document.setName(fileName);
        document.setPath("S3");
        document.setContentType(file.getContentType());

        String objectKey = s3Service.uploadFileToS3(file.getBytes(), fileName);
        log.info("Document: {} saved on S3 with objectKey: {}", fileName, objectKey);
        document.setObjectKey(objectKey);

        return documentRepository.save(document);
    }

    public Document getDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
    }


    public EmailResponse sendDocumentToEmail(UUID id, String recipientEmail, String mailSubject, String mailBody)
            throws IOException {
        Document document = getDocument(id);
        byte[] downloadedDocument = s3Service.downloadFileFromS3(document.getObjectKey());

        emailService.sendEmailWithAttachment(recipientEmail, mailSubject, mailBody, downloadedDocument, document.getName());

        return new EmailResponse("Email sent successfully", document.getName(), recipientEmail);
    }
}
