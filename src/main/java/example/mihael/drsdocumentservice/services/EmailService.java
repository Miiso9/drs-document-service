package example.mihael.drsdocumentservice.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.sender.email}")
    private String senderEmail;

    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachmentData, String attachmentName) throws IOException {
        Email from = new Email(senderEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", body);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addContent(content);

        Personalization personalization = new Personalization();
        personalization.addTo(toEmail);
        mail.addPersonalization(personalization);

        // Add attachment
        Attachments attachments = new Attachments();
        attachments.setContent(Base64.getEncoder().encodeToString(attachmentData));
        attachments.setType("application/octet-stream");
        attachments.setFilename(attachmentName);
        attachments.setDisposition("attachment");
        mail.addAttachments(attachments);

        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        try {
            Response response = sendGrid.api(request);
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Body: {}", response.getBody());
            log.info("Response Headers: {}", response.getHeaders());
        } catch (IOException ex) {
            log.error("Error sending email: " + ex.getMessage(), ex);
            throw ex;
        }
    }
}
