package org.veri.be.unit.global.storage

import io.github.miensoap.s3.core.ExtendedS3Presigner
import io.github.miensoap.s3.core.post.dto.PresignedPostForm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.veri.be.global.storage.service.AwsStorageService
import org.veri.be.global.storage.service.StorageKeyGenerator
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Utilities
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URI
import java.time.Duration
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
class AwsStorageServiceTest {

    @org.mockito.Mock
    private lateinit var s3Client: S3Client

    @org.mockito.Mock
    private lateinit var s3Presigner: S3Presigner

    @org.mockito.Mock
    private lateinit var extendedS3Presigner: ExtendedS3Presigner

    @org.mockito.Mock
    private lateinit var storageKeyGenerator: StorageKeyGenerator

    @org.mockito.Mock
    private lateinit var presignedPutObjectRequest: PresignedPutObjectRequest

    @org.mockito.Mock
    private lateinit var s3Utilities: S3Utilities

    private lateinit var awsStorageService: AwsStorageService

    @BeforeEach
    fun setUp() {
        awsStorageService = AwsStorageService(
            s3Client,
            s3Presigner,
            extendedS3Presigner,
            storageKeyGenerator
        )
        ReflectionTestUtils.setField(awsStorageService, "bucket", "test-bucket")
        lenient().`when`(s3Client.utilities()).thenReturn(s3Utilities)
    }

    @Nested
    @DisplayName("generatePresignedUrl")
    inner class GeneratePresignedUrl {

        @Test
        @DisplayName("presigned URL과 public URL을 반환한다")
        fun returnsPresignedUrl() {
            given(storageKeyGenerator.generate("image/png", "public")).willReturn("public/key.png")
            given(s3Presigner.presignPutObject(any(PutObjectPresignRequest::class.java))).willReturn(
                presignedPutObjectRequest
            )
            given(presignedPutObjectRequest.url()).willReturn(URI.create("https://example.com/presigned").toURL())
            given(s3Utilities.getUrl(any<Consumer<GetUrlRequest.Builder>>()))
                .willReturn(URI.create("https://example.com/public/key.png").toURL())

            val response = awsStorageService.generatePresignedUrl(
                "image/png",
                100L,
                "public",
                Duration.ofMinutes(5)
            )

            assertThat(response.presignedUrl()).isEqualTo("https://example.com/presigned")
            assertThat(response.publicUrl()).isEqualTo("https://example.com/public/key.png")
        }

        @Test
        @DisplayName("기본 prefix와 만료 시간으로 presigned URL을 반환한다")
        fun returnsPresignedUrlWithDefaultSettings() {
            given(storageKeyGenerator.generate("image/png", "public")).willReturn("public/key.png")
            given(s3Presigner.presignPutObject(any(PutObjectPresignRequest::class.java))).willReturn(
                presignedPutObjectRequest
            )
            given(presignedPutObjectRequest.url()).willReturn(URI.create("https://example.com/presigned").toURL())
            given(s3Utilities.getUrl(any<Consumer<GetUrlRequest.Builder>>()))
                .willReturn(URI.create("https://example.com/public/key.png").toURL())

            val response = awsStorageService.generatePresignedUrlOfDefault(
                "image/png",
                100L
            )

            assertThat(response.presignedUrl()).isEqualTo("https://example.com/presigned")
            assertThat(response.publicUrl()).isEqualTo("https://example.com/public/key.png")
        }
    }

    @Nested
    @DisplayName("generatePresignedPost")
    inner class GeneratePresignedPost {

        @Test
        @DisplayName("presigned post form을 반환한다")
        fun returnsPresignedPostForm() {
            val form = PresignedPostForm("https://example.com", mapOf())
            given(storageKeyGenerator.generate("image/*", "public")).willReturn("public/key.png")
            given(extendedS3Presigner.presignPostObject(any())).willReturn(form)

            val result = awsStorageService.generatePresignedPost(
                "image/*",
                1024L,
                "public",
                Duration.ofMinutes(5)
            )

            assertThat(result.url()).isEqualTo(form.url())
            assertThat(result.fields()).isEqualTo(form.formFields())
        }
    }
}
