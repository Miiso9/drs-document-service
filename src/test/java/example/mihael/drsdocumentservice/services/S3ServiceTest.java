package example.mihael.drsdocumentservice.services;

import example.mihael.drsdocumentservice.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    @Captor
    private ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

    private final String testBucket = "test-bucket";
    private final String testKey = "test_file_12345678.docx";
    private final byte[] testContent = "test content".getBytes();

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(s3Client, testBucket);
    }

    @Test
    void should_upload_file_to_S3() throws IOException {
        // Given
        String originalFilename = "test_file.docx";

        // When
        String resultKey = s3Service.uploadFileToS3(testContent, originalFilename);

        // Then
        assertNotNull(resultKey);
        assertTrue(resultKey.startsWith("test_file_"));
        assertTrue(resultKey.endsWith(".docx"));
        assertEquals(23, resultKey.length());

        verify(s3Client).putObject(putObjectRequestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = putObjectRequestCaptor.getValue();
        assertTrue(request.key().matches("test_file_\\w{8}\\.docx"));
    }

    @Test
    void should_throw_exception_on_upload_file_to_s3() {
        // Given
        doThrow(S3Exception.builder().message("Mock Error").build())
                .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        assertThrows(IOException.class, () -> s3Service.uploadFileToS3(testContent, "test.docx"));
    }

    @Test
    void should_download_file_from_s3() throws ResourceNotFoundException {
        // Given
        GetObjectResponse response = GetObjectResponse.builder().build();
        ResponseBytes<GetObjectResponse> responseBytes = ResponseBytes.fromByteArray(response, testContent);

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(responseBytes);

        // When
        byte[] result = s3Service.downloadFileFromS3(testKey);

        // Then
        assertArrayEquals(testContent, result);
        verify(s3Client).getObjectAsBytes(argThat((GetObjectRequest req) ->
                req.bucket().equals(testBucket) &&
                        req.key().equals(testKey)
        ));
    }

    @Test
    void should_list_files_in_bucket() throws IOException {
        // Given
        S3Object s3Object1 = S3Object.builder().key("test_file1.txt").build();
        S3Object s3Object2 = S3Object.builder().key("test_file2.txt").build();

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .contents(s3Object1, s3Object2)
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(response);

        // When
        List<String> files = s3Service.listFilesOfBucket();

        // Then
        assertEquals(2, files.size());
        assertIterableEquals(List.of("test_file1.txt", "test_file2.txt"), files);
        verify(s3Client).listObjectsV2(argThat((ListObjectsV2Request  req) ->
                req.bucket().equals(testBucket)
        ));
    }

    @Test
    void should_throw_exception_on_download_file_from_s3() {
        // Arrange
        S3Exception s3Exception = (S3Exception) S3Exception.builder().message("S3 Error").build();
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(s3Exception);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> s3Service.downloadFileFromS3(testKey));

        assertEquals("Failed to download Word document from S3", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_list_all_files_from_s3() {
        // Arrange
        S3Exception s3Exception = (S3Exception) S3Exception.builder().message("S3 Error").build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(s3Exception);

        // Act & Assert
        IOException exception = assertThrows(IOException.class,
                () -> s3Service.listFilesOfBucket());

        assertEquals("Failed to list files in S3 bucket", exception.getMessage());
    }

}
