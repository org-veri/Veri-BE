package org.veri.be.unit.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.controller.dto.CardConverter;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.controller.dto.response.CardVisibilityUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.support.assertion.ExceptionAssertions;
import org.veri.be.domain.card.service.CardCommandService;

@ExtendWith(MockitoExtension.class)
class CardCommandServiceTest {

    @Mock
    CardRepository cardRepository;

    @Mock
    ReadingRepository readingRepository;

    @Mock
    StorageService storageService;

    @Mock
    CardConverter cardConverter;

    CardCommandService cardCommandService;

    @Captor
    ArgumentCaptor<Card> cardCaptor;

    @BeforeEach
    void setUp() {
        cardCommandService = new CardCommandService(
                cardRepository,
                readingRepository,
                storageService,
                cardConverter
        );
    }

    @Nested
    @DisplayName("createCard")
    class CreateCard {

        @Test
        @DisplayName("독서가 비공개면 카드도 비공개로 저장된다")
        void forcesPrivateWhenReadingPrivate() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = Reading.builder().id(10L).isPublic(false).build();

            given(readingRepository.findById(10L)).willReturn(Optional.of(reading));
            given(cardRepository.save(any(Card.class))).willAnswer(invocation -> {
                Card saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            Long id = cardCommandService.createCard(member, "content", "https://example.com/card.png", 10L, true);

            verify(cardRepository).save(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getIsPublic()).isFalse();
            assertThat(id).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("updateCard")
    class UpdateCard {

        @Test
        @DisplayName("카드를 수정하고 응답을 반환한다")
        void updatesCard() {
            Member member = member(1L, "member@test.com", "member");
            Card card = Card.builder()
                    .id(1L)
                    .member(member)
                    .content("before")
                    .image("https://example.com/before.png")
                    .build();
            Card updated = card.updateContent("after", "https://example.com/after.png", member);
            CardUpdateResponse response = new CardUpdateResponse(1L, "after", "https://example.com/after.png", null, null, null);

            given(cardRepository.findById(1L)).willReturn(Optional.of(card));
            given(cardRepository.save(any(Card.class))).willReturn(updated);
            given(cardConverter.toCardUpdateResponse(updated)).willReturn(response);

            CardUpdateResponse result = cardCommandService.updateCard(member, 1L, "after", "https://example.com/after.png");

            assertThat(result).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("modifyVisibility")
    class ModifyVisibility {

        @Test
        @DisplayName("공개 상태를 변경한다")
        void updatesVisibility() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = Reading.builder().id(10L).isPublic(true).build();
            Card card = Card.builder().id(1L).member(member).reading(reading).isPublic(false).build();

            given(cardRepository.findById(1L)).willReturn(Optional.of(card));

            CardVisibilityUpdateResponse result = cardCommandService.modifyVisibility(member, 1L, true);

            verify(cardRepository).save(cardCaptor.capture());
            assertThat(cardCaptor.getValue().getIsPublic()).isTrue();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.isPublic()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteCard")
    class DeleteCard {

        @Test
        @DisplayName("카드를 삭제한다")
        void deletesCard() {
            Member member = member(1L, "member@test.com", "member");
            Card card = Card.builder().id(1L).member(member).build();

            given(cardRepository.findById(1L)).willReturn(Optional.of(card));

            cardCommandService.deleteCard(member, 1L);

            verify(cardRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("getPresignedUrlForOcr")
    class GetPresignedUrlForOcr {

        @Test
        @DisplayName("용량이 크면 예외가 발생한다")
        void throwsWhenTooLarge() {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 3 * 1024 * 1024L + 1);

            ExceptionAssertions.assertApplicationException(
                    () -> cardCommandService.getPresignedUrlForOcr(request),
                    CardErrorInfo.IMAGE_TOO_LARGE
            );
        }

        @Test
        @DisplayName("이미지 타입이 아니면 예외가 발생한다")
        void throwsWhenUnsupportedType() {
            PresignedUrlRequest request = new PresignedUrlRequest("application/pdf", 100);

            ExceptionAssertions.assertApplicationException(
                    () -> cardCommandService.getPresignedUrlForOcr(request),
                    CardErrorInfo.UNSUPPORTED_IMAGE_TYPE
            );
        }

        @Test
        @DisplayName("OCR용 Presigned URL을 반환한다")
        void returnsPresignedUrl() {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 100);
            PresignedUrlResponse response = new PresignedUrlResponse("https://example.com/presigned", "https://example.com/public");

            given(storageService.generatePresignedUrl(eq("image/png"), eq(100L), eq("public/ocr"), eq(Duration.ofMinutes(5))))
                    .willReturn(response);

            PresignedUrlResponse result = cardCommandService.getPresignedUrlForOcr(request);

            assertThat(result).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("getPresignedUrl")
    class GetPresignedUrl {

        @Test
        @DisplayName("용량이 크면 예외가 발생한다")
        void throwsWhenTooLarge() {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 3 * 1024 * 1024L + 1);

            ExceptionAssertions.assertApplicationException(
                    () -> cardCommandService.getPresignedUrl(request),
                    CardErrorInfo.IMAGE_TOO_LARGE
            );
        }

        @Test
        @DisplayName("이미지 타입이 아니면 예외가 발생한다")
        void throwsWhenUnsupportedType() {
            PresignedUrlRequest request = new PresignedUrlRequest("application/pdf", 100);

            ExceptionAssertions.assertApplicationException(
                    () -> cardCommandService.getPresignedUrl(request),
                    CardErrorInfo.UNSUPPORTED_IMAGE_TYPE
            );
        }

        @Test
        @DisplayName("이미지 업로드용 Presigned URL을 반환한다")
        void returnsPresignedUrl() {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 100);
            PresignedUrlResponse response = new PresignedUrlResponse("https://example.com/presigned", "https://example.com/public");

            given(storageService.generatePresignedUrl(eq("image/png"), eq(100L), eq("public"), eq(Duration.ofMinutes(5))))
                    .willReturn(response);

            PresignedUrlResponse result = cardCommandService.getPresignedUrl(request);

            assertThat(result).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("getPresignedPost")
    class GetPresignedPost {

        @Test
        @DisplayName("Presigned Post 폼을 반환한다")
        void returnsPresignedPost() {
            PresignedPostForm form = new PresignedPostForm("https://example.com", java.util.Map.of());
            given(storageService.generatePresignedPost(eq("image/*"), eq(3 * 1024 * 1024L), eq("public"), eq(Duration.ofMinutes(5))))
                    .willReturn(form);

            PresignedPostForm result = cardCommandService.getPresignedPost();

            assertThat(result).isEqualTo(form);
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
