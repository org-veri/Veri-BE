package org.veri.be.unit.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.veri.be.image.service.ImageRepository
import org.veri.be.image.service.ImageQueryService
import org.veri.be.global.response.PageResponse

@ExtendWith(MockitoExtension::class)
class ImageQueryServiceTest {

    @org.mockito.Mock
    private lateinit var imageRepository: ImageRepository

    private lateinit var imageQueryService: ImageQueryService

    @BeforeEach
    fun setUp() {
        imageQueryService = ImageQueryService(imageRepository)
    }

    @Nested
    @DisplayName("fetchUploadedImages")
    inner class FetchUploadedImages {

        @Test
        @DisplayName("빈 결과면 empty 응답을 반환한다")
        fun returnsEmptyResponse() {
            val pageable = PageRequest.of(0, 10)
            given(imageRepository.findByMemberId(1L, pageable)).willReturn(Page.empty())

            val response: PageResponse<List<String>> = imageQueryService.fetchUploadedImages(1L, pageable)

            assertThat(response.content()).isNull()
            assertThat(response.page()).isEqualTo(1)
            assertThat(response.size()).isEqualTo(10)
            assertThat(response.totalElements()).isZero()
            assertThat(response.totalPages()).isZero()
        }

        @Test
        @DisplayName("이미지 URL 목록을 페이징 응답으로 반환한다")
        fun returnsPageResponse() {
            val pageable = PageRequest.of(1, 2)
            val page = PageImpl(
                listOf("https://example.com/1.png", "https://example.com/2.png"),
                pageable,
                4
            )
            given(imageRepository.findByMemberId(1L, pageable)).willReturn(page)

            val response: PageResponse<List<String>> = imageQueryService.fetchUploadedImages(1L, pageable)

            assertThat(response.content()).hasSize(2)
            assertThat(response.page()).isEqualTo(2)
            assertThat(response.size()).isEqualTo(2)
            assertThat(response.totalElements()).isEqualTo(4)
            assertThat(response.totalPages()).isEqualTo(2)
        }
    }
}
