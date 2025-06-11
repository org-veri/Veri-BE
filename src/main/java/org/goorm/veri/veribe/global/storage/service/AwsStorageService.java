package org.goorm.veri.veribe.global.storage.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "cloud.storage.platform", havingValue = "aws")
@RequiredArgsConstructor
public class AwsStorageService implements StorageService {

    private final StorageUtil storageUtil;

    @Value("${cloud.aws.s3.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.s3.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    @Override
    public PresignedUrlResponse generatePresignedUrl(String contentType, Duration duration) {
        String key = storageUtil.generateUniqueKey(contentType);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(1_048_576L) // 1MB, 임시
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
            return new PresignedUrlResponse(presignedRequest.url().toString(), getPublicUrl(key));
        }
    }

    private String getPublicUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
    }
}
