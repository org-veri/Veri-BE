package org.goorm.veri.veribe.global.storage.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "cloud.storage.platform", havingValue = "aws")
@RequiredArgsConstructor
public class AwsStorageService implements StorageService {

    @Value("${cloud.aws.s3.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.s3.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    private S3Client s3Client;

    @PostConstruct
    public void initS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Override
    public PresignedUrlResponse generatePresignedUrl(
            String contentType,
            Duration duration,
            long fileSize,
            String prefix
    ) {
        String key = StorageUtil.generateUniqueKey(contentType, prefix);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
//                .contentLength(fileSize) #10
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(objectRequest)
                .build();

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)))
                .build()) {

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            return new PresignedUrlResponse(presignedRequest.url().toString(), getPublicUrl(key), key);
        }
    }

    public void uploadImageToS3(BufferedImage image, String imageKey, String fileFormat) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(imageKey)
                    .contentType(fileFormat)
                    .build();


            ImageIO.write(image, fileFormat, os);
            byte[] bytes = os.toByteArray();


            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));


        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.UPLOAD_FAILED);
        }
    }
    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
    }
}
