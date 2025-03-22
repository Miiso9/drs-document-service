package example.mihael.drsdocumentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class DrsDocumentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrsDocumentServiceApplication.class, args);
    }

}
