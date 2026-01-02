package org.veri.be.unit.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.image.entity.Image
import org.veri.be.domain.image.exception.ImageErrorCode
import org.veri.be.domain.image.repository.ImageRepository
import org.veri.be.domain.image.service.ImageCommandService
import org.veri.be.domain.image.service.OcrService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.lib.exception.ApplicationException
import org.veri.be.support.assertion.ExceptionAssertions

@ExtendWith(MockitoExtension::class)
class ImageCommandServiceTest {

    @org.mockito.Mock
    private lateinit var imageRepository: ImageRepository

    @org.mockito.Mock
    private lateinit var ocrService: OcrService

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var imageCommandService: ImageCommandService

    @org.mockito.Captor
    private lateinit var imageCaptor: ArgumentCaptor<Image>

    @BeforeEach
    fun setUp() {
        imageCommandService = ImageCommandService(imageRepository, ocrService, memberRepository)
    }

    @Nested
    @DisplayName("processWithMistral")
    inner class ProcessWithMistral {

        @Test
        @DisplayName("이미지 저장 후 OCR 결과를 반환한다")
        fun returnsOcrResult() {
            val member = member(1L, "member@test.com", "member")
            given(ocrService.extract("https://example.com/image.png")).willReturn("text")
            given(memberRepository.getReferenceById(1L)).willReturn(member)
            given(imageRepository.save(any(Image::class.java))).willAnswer { invocation -> invocation.getArgument(0) }

            val result = imageCommandService.processWithMistral(member.id, "https://example.com/image.png")

            assertThat(result).isEqualTo("text")
            verify(imageRepository).save(imageCaptor.capture())
            assertThat(imageCaptor.value.imageUrl).isEqualTo("https://example.com/image.png")
            assertThat(imageCaptor.value.member).isEqualTo(member)
        }

        @Test
        @DisplayName("예상치 못한 예외는 내부 서버 예외로 변환된다")
        fun wrapsUnexpectedException() {
            val member = member(1L, "member@test.com", "member")
            given(ocrService.extract("https://example.com/image.png"))
                .willThrow(RuntimeException("boom"))
            given(memberRepository.getReferenceById(1L)).willReturn(member)

            ExceptionAssertions.assertApplicationException(
                { imageCommandService.processWithMistral(member.id, "https://example.com/image.png") },
                ImageErrorCode.OCR_PROCESSING_FAILED
            )
        }

        @Test
        @DisplayName("ApplicationException은 그대로 전달된다")
        fun rethrowsApplicationException() {
            val member = member(1L, "member@test.com", "member")
            given(ocrService.extract("https://example.com/image.png"))
                .willThrow(ApplicationException.of(ImageErrorCode.OCR_PROCESSING_FAILED))
            given(memberRepository.getReferenceById(1L)).willReturn(member)

            ExceptionAssertions.assertApplicationException(
                { imageCommandService.processWithMistral(member.id, "https://example.com/image.png") },
                ImageErrorCode.OCR_PROCESSING_FAILED
            )
        }
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
