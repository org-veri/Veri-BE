package org.veri.be.unit.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.image.entity.Image;
import org.veri.be.domain.image.exception.ImageErrorInfo;
import org.veri.be.domain.image.repository.ImageRepository;
import org.veri.be.domain.image.service.ImageCommandService;
import org.veri.be.domain.image.service.OcrService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.lib.exception.http.InternalServerException;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class ImageCommandServiceTest {

    @Mock
    ImageRepository imageRepository;

    @Mock
    OcrService ocrService;

    ImageCommandService imageCommandService;

    @Captor
    ArgumentCaptor<Image> imageCaptor;

    @BeforeEach
    void setUp() {
        imageCommandService = new ImageCommandService(imageRepository, ocrService);
    }

    @Nested
    @DisplayName("processWithMistral")
    class ProcessWithMistral {

        @Test
        @DisplayName("이미지 저장 후 OCR 결과를 반환한다")
        void returnsOcrResult() {
            Member member = member(1L, "member@test.com", "member");
            given(ocrService.extract("https://example.com/image.png")).willReturn("text");
            given(imageRepository.save(any(Image.class))).willAnswer(invocation -> invocation.getArgument(0));

            String result = imageCommandService.processWithMistral(member, "https://example.com/image.png");

            assertThat(result).isEqualTo("text");
            verify(imageRepository).save(imageCaptor.capture());
            assertThat(imageCaptor.getValue().getImageUrl()).isEqualTo("https://example.com/image.png");
            assertThat(imageCaptor.getValue().getMember()).isEqualTo(member);
        }

        @Test
        @DisplayName("예상치 못한 예외는 내부 서버 예외로 변환된다")
        void wrapsUnexpectedException() {
            Member member = member(1L, "member@test.com", "member");
            given(ocrService.extract("https://example.com/image.png"))
                    .willThrow(new RuntimeException("boom"));

            ExceptionAssertions.assertApplicationException(
                    () -> imageCommandService.processWithMistral(member, "https://example.com/image.png"),
                    ImageErrorInfo.OCR_PROCESSING_FAILED
            );
        }

        @Test
        @DisplayName("InternalServerException은 그대로 전달된다")
        void rethrowsInternalServerException() {
            Member member = member(1L, "member@test.com", "member");
            given(ocrService.extract("https://example.com/image.png"))
                    .willThrow(new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED));

            ExceptionAssertions.assertApplicationException(
                    () -> imageCommandService.processWithMistral(member, "https://example.com/image.png"),
                    ImageErrorInfo.OCR_PROCESSING_FAILED
            );
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
