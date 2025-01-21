package example.mihael.drsdocumentservice.dto;
import lombok.*;

@Data
@AllArgsConstructor
public class EmailResponse {
    private String message;
    private String documentName;
    private String recipientEmail;
}
