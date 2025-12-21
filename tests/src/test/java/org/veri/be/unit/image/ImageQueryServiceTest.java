package org.veri.be.unit.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.veri.be.domain.image.repository.ImageRepository;
import org.veri.be.domain.image.service.ImageQueryService;
import org.veri.be.global.response.PageResponse;

@ExtendWith(MockitoExtension.class)
class ImageQueryServiceTest {

    @Mock
    ImageRepository imageRepository;

    ImageQueryService imageQueryService;

    @BeforeEach
    void setUp() {
        imageQueryService = new ImageQueryService(imageRepository);
    }

    @Nested
    @DisplayName("fetchUploadedImages")
    class FetchUploadedImages {

        @Test
        @DisplayName("빈 결과면 empty 응답을 반환한다")
        void returnsEmptyResponse() {
            PageRequest pageable = PageRequest.of(0, 10);
            given(imageRepository.findByMemberId(1L, pageable)).willReturn(Page.empty());

            PageResponse<List<String>> response = imageQueryService.fetchUploadedImages(1L, pageable);

            assertThat(response.content()).isNull();
            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isZero();
            assertThat(response.totalPages()).isZero();
        }

        @Test
        @DisplayName("이미지 URL 목록을 페이징 응답으로 반환한다")
        void returnsPageResponse() {
            PageRequest pageable = PageRequest.of(1, 2);
            Page<String> page = new PageImpl<>(
                    List.of("https://example.com/1.png", "https://example.com/2.png"),
                    pageable,
                    4
            );
            given(imageRepository.findByMemberId(1L, pageable)).willReturn(page);

            PageResponse<List<String>> response = imageQueryService.fetchUploadedImages(1L, pageable);

            assertThat(response.content()).hasSize(2);
            assertThat(response.page()).isEqualTo(2);
            assertThat(response.size()).isEqualTo(2);
            assertThat(response.totalElements()).isEqualTo(4);
            assertThat(response.totalPages()).isEqualTo(2);
        }
    }
}
