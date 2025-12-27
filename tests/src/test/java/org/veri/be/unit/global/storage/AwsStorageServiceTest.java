package org.veri.be.unit.global.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import io.github.miensoap.s3.core.ExtendedS3Presigner;
import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import java.net.URL;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.global.storage.dto.PresignedPostFormResponse;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.AwsStorageService;
import org.veri.be.global.storage.service.StorageKeyGenerator;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
class AwsStorageServiceTest {

    @Mock
    S3Client s3Client;

    @Mock
    S3Presigner s3Presigner;

    @Mock
    ExtendedS3Presigner extendedS3Presigner;

    @Mock
    StorageKeyGenerator storageKeyGenerator;

    @Mock
    PresignedPutObjectRequest presignedPutObjectRequest;

    @Mock
    S3Utilities s3Utilities;

    AwsStorageService awsStorageService;

    @BeforeEach
    void setUp() {
        awsStorageService = new AwsStorageService(
                s3Client,
                s3Presigner,
                extendedS3Presigner,
                storageKeyGenerator
        );
        ReflectionTestUtils.setField(awsStorageService, "bucket", "test-bucket");
        org.mockito.Mockito.lenient().when(s3Client.utilities()).thenReturn(s3Utilities);
    }

    @Nested
    @DisplayName("generatePresignedUrl")
    class GeneratePresignedUrl {

        @Test
        @DisplayName("presigned URL과 public URL을 반환한다")
        void returnsPresignedUrl() throws Exception {
            given(storageKeyGenerator.generate("image/png", "public")).willReturn("public/key.png");
            given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presignedPutObjectRequest);
            given(presignedPutObjectRequest.url()).willReturn(new URL("https://example.com/presigned"));
            given(s3Utilities.getUrl(any(java.util.function.Consumer.class)))
                    .willReturn(new URL("https://example.com/public/key.png"));

            PresignedUrlResponse response = awsStorageService.generatePresignedUrl(
                    "image/png",
                    100L,
                    "public",
                    Duration.ofMinutes(5)
            );

            assertThat(response.presignedUrl()).isEqualTo("https://example.com/presigned");
            assertThat(response.publicUrl()).isEqualTo("https://example.com/public/key.png");
        }

        @Test
        @DisplayName("기본 prefix와 만료 시간으로 presigned URL을 반환한다")
        void returnsPresignedUrlWithDefaultSettings() throws Exception {
            given(storageKeyGenerator.generate("image/png", "public")).willReturn("public/key.png");
            given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).willReturn(presignedPutObjectRequest);
            given(presignedPutObjectRequest.url()).willReturn(new URL("https://example.com/presigned"));
            given(s3Utilities.getUrl(any(java.util.function.Consumer.class)))
                    .willReturn(new URL("https://example.com/public/key.png"));

            PresignedUrlResponse response = awsStorageService.generatePresignedUrlOfDefault(
                    "image/png",
                    100L
            );

            assertThat(response.presignedUrl()).isEqualTo("https://example.com/presigned");
            assertThat(response.publicUrl()).isEqualTo("https://example.com/public/key.png");
        }
    }

    @Nested
    @DisplayName("generatePresignedPost")
    class GeneratePresignedPost {

        @Test
        @DisplayName("presigned post form을 반환한다")
        void returnsPresignedPostForm() {
            PresignedPostForm form = new PresignedPostForm("https://example.com", java.util.Map.of());
            given(storageKeyGenerator.generate("image/*", "public")).willReturn("public/key.png");
            given(extendedS3Presigner.presignPostObject(any())).willReturn(form);

            PresignedPostFormResponse result = awsStorageService.generatePresignedPost(
                    "image/*",
                    1024L,
                    "public",
                    Duration.ofMinutes(5)
            );

            assertThat(result.url()).isEqualTo(form.url());
            assertThat(result.fields()).isEqualTo(form.formFields());
        }
    }
}
