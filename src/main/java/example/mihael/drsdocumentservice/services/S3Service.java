package example.mihael.drsdocumentservice.services;

import example.mihael.drsdocumentservice.exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    public S3Service(S3Client s3Client, @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadFileToS3(byte[] wordBytes, String originalFilename) throws IOException {
        try {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String objectKey = originalFilename.replace(".","_" + uuid + ".");

            try (InputStream inputStream = new ByteArrayInputStream(wordBytes)) {
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build(), RequestBody.fromInputStream(inputStream, wordBytes.length));
            }

            return objectKey;
        } catch (S3Exception e) {
            throw new IOException("Failed to upload Word document to S3", e);
        }
    }

    @Cacheable(cacheNames = "wordDocument", key = "#objectKey")
    public byte[] downloadFileFromS3(String objectKey) throws ResourceNotFoundException {
        try {
            log.info("Retrieving Word document from storage with id: {}", objectKey);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            return objectBytes.asByteArray();
        } catch (S3Exception e) {
            log.error("Failed to download Word document from S3", e);
            throw new ResourceNotFoundException("Failed to download Word document from S3");
        }
    }

    public List<String> listFilesOfBucket() throws IOException {
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

            List<S3Object> s3Objects = listObjectsResponse.contents();

            return s3Objects.stream()
                    .map(S3Object::key)
                    .toList();
        } catch (S3Exception e) {
            throw new IOException("Failed to list files in S3 bucket", e);
        }
    }
}
