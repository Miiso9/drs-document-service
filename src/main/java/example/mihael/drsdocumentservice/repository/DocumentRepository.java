package example.mihael.drsdocumentservice.repository;

import example.mihael.drsdocumentservice.models.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

}
