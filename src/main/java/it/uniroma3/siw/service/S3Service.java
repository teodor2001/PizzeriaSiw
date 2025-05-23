package it.uniroma3.siw.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String regionString;

    @Value("${my.aws.access-key-id}")
    private String awsAccessKey;

    @Value("${my.aws.secret-access-key}")
    private String awsSecretKey;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        try {
            Region region = Region.of(regionString);

            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey));

            this.s3Client = S3Client.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .build();
            logger.info("S3Client initialized successfully for region {} and bucket {} using provided credentials", regionString, bucketName);
        } catch (Exception e) {
            logger.error("Error initializing S3Client: {}. AWS Access Key loaded: {}, AWS Secret Key loaded: {}", 
                         e.getMessage(), (awsAccessKey != null && !awsAccessKey.isEmpty()), (awsSecretKey != null && !awsSecretKey.isEmpty()), e);
            if (awsAccessKey == null || awsAccessKey.isEmpty() || awsSecretKey == null || awsSecretKey.isEmpty()) {
                logger.error("AWS credentials were not loaded correctly from properties/secrets.");
            }
        }
    }

    public String uploadFile(String basePath, String originalFileNameFromUser, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Il file da caricare non può essere nullo o vuoto.");
        }
        long maxSize = 5 * 1024 * 1024; 
        if (file.getSize() > maxSize) {
            String sizeInMB = String.format("%.2f", file.getSize() / (1024.0 * 1024.0));
            throw new IllegalArgumentException("L'immagine è troppo grande. Dimensione attuale: " + sizeInMB + "MB. Dimensione massima consentita: 5MB.");
        }
        if (s3Client == null) {
            logger.error("S3Client is null in uploadFile. Check initialization and credentials loading.");
            throw new IllegalStateException("S3Client non è stato inizializzato correttamente. Controllare la configurazione AWS e i log.");
        }
        String sanitizedOriginalName = StringUtils.cleanPath(originalFileNameFromUser);
        String extension = "";
        int i = sanitizedOriginalName.lastIndexOf('.');
        if (i > 0) {
            extension = sanitizedOriginalName.substring(i);
        }
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        String objectKey = (basePath != null && !basePath.trim().isEmpty() ? basePath.trim() + "/" : "") + uniqueFileName;
        logger.info("Attempting to upload file to S3. Bucket: '{}', ObjectKey: '{}'", bucketName, objectKey);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info("File uploaded successfully to S3. ObjectKey: '{}'", objectKey);
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            URL url = s3Client.utilities().getUrl(getUrlRequest);
            logger.info("Generated S3 URL: {}", url.toString());
            return url.toString();
        } catch (S3Exception e) {
            logger.error("Errore durante l'upload del file su S3. ObjectKey: '{}', AWS Error: {}", objectKey, e.awsErrorDetails().errorMessage(), e);
            throw new IOException("Errore durante l'upload del file su S3: " + e.awsErrorDetails().errorMessage(), e);
        } catch (IOException e) {
            logger.error("Errore di IO durante la preparazione dell'upload su S3. ObjectKey: '{}'", objectKey, e);
            throw e;
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            logger.warn("deleteFile: URL nullo o vuoto.");
            return;
        }
        if (s3Client == null) {
            logger.warn("deleteFile: S3Client non inizializzato. Impossibile eliminare il file.");
            return;
        }
        try {
            URL url = new URL(fileUrl);
            String objectKey = url.getPath().substring(1);
            
            String hostBucket = url.getHost().split("\\.")[0];
            if (!hostBucket.equals(bucketName) && !objectKey.startsWith(bucketName + "/")) {
                 logger.warn("deleteFile: L'URL fornito ('{}') non sembra appartenere al bucket configurato ('{}'). Host bucket: {}", fileUrl, bucketName, hostBucket);

                 if (!objectKey.startsWith(bucketName + "/")) {
                    logger.error("Tentativo di eliminazione da un bucket non corrispondente o formato URL S3 non gestito. URL: {}, Bucket configurato: {}", fileUrl, bucketName);
                    return;
                 }
            }
            
            if (objectKey.startsWith(bucketName + "/")) {
                objectKey = objectKey.substring((bucketName + "/").length());
            }

            logger.info("Attempting to delete file from S3. Bucket: '{}', ObjectKey: '{}'", bucketName, objectKey);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted successfully from S3. ObjectKey: '{}'", objectKey);
        } catch (S3Exception e) {
            logger.error("Errore durante l'eliminazione del file da S3. URL: '{}', AWS Error: {}", fileUrl, e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            logger.error("Errore generico durante il tentativo di eliminare il file da S3. URL: '{}'", fileUrl, e);
        }
    }
}
