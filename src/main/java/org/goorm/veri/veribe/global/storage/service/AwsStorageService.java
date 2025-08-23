package org.goorm.veri.veribe.global.storage.service;

import io.github.miensoap.s3.core.ExtendedS3Presigner;
import io.github.miensoap.s3.core.post.dto.PostObjectPresignRequest;
import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import io.github.miensoap.s3.core.post.s3policy.PostConditions;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AwsStorageService implements StorageService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ExtendedS3Presigner extendedS3Presigner;

    @Override
    public PresignedUrlResponse generatePresignedUrlOfDefault(String contentType, long contentLength) {
        String prefix = "public";
        Duration duration = Duration.ofMinutes(5);
        return generatePresignedUrl(contentType, contentLength, prefix, duration);
    }

    @Override
    public PresignedUrlResponse generatePresignedUrl(
            String contentType,
            long contentLength,
            String prefix,
            Duration duration
    ) {
        String key = StorageUtil.generateUniqueKey(contentType, prefix);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentLength(contentLength)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(presignedRequest.url().toString(), getPublicUrl(key));
    }

    public PresignedPostForm generatePresignedPost(
            String contentType,
            long fileSize,
            String prefix,
            Duration duration
    ) {
        String key = StorageUtil.generateUniqueKey(contentType, prefix);

        PostConditions conditions = PostConditions.builder()
                .contentType(contentType)
                .maxSize(fileSize)
                .build();

        PostObjectPresignRequest presignRequest = PostObjectPresignRequest.builder()
                .bucket(bucket)
                .key(key)
                .conditions(conditions)
                .signatureDuration(duration)
                .build();

        return extendedS3Presigner.presignPostObject(presignRequest);
    }

    private String getPublicUrl(String key) {
        // 주입받은 s3Client를 사용하여 URL을 생성합니다.
        return s3Client.utilities().getUrl(b -> b.bucket(bucket).key(key)).toString();
    }
}
