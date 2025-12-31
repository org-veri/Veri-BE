package org.veri.be.slice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.lenient
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.image.ImageController
import org.veri.be.image.service.ImageCommandService
import org.veri.be.image.service.ImageQueryService
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.auth.context.AuthenticatedMemberResolver
import org.veri.be.lib.auth.context.MemberContext
import org.veri.be.member.auth.context.MemberRequestContext
import org.veri.be.member.auth.context.ThreadLocalCurrentMemberAccessor
import org.veri.be.member.service.MemberQueryService
import org.veri.be.global.response.PageResponse
import org.veri.be.lib.response.ApiResponseAdvice
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ImageControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var imageCommandService: ImageCommandService

    @org.mockito.Mock
    private lateinit var imageQueryService: ImageQueryService

    @org.mockito.Mock
    private lateinit var memberQueryService: MemberQueryService

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        member = Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()
        MemberContext.setCurrentMemberId(member.id)
        lenient().`when`(memberQueryService.findOptionalById(member.id)).thenReturn(Optional.of(member))

        val controller = ImageController(imageCommandService, imageQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(ThreadLocalCurrentMemberAccessor(memberQueryService))
            )
            .build()
    }

    @AfterEach
    fun tearDown() {
        MemberContext.clear()
        MemberRequestContext.clear()
    }

    @Nested
    @DisplayName("POST /api/v0/images/ocr")
    inner class OcrImageV0 {

        @Test
        @DisplayName("이미지 OCR 결과를 반환한다")
        fun returnsOcrResult() {
            given(imageCommandService.processWithMistral(member, "https://example.com/image.png"))
                .willReturn("text")

            mockMvc.perform(
                post("/api/v0/images/ocr")
                    .param("imageUrl", "https://example.com/image.png")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("text"))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/images/ocr")
    inner class OcrImageV1 {

        @Test
        @DisplayName("이미지 OCR 결과를 반환한다")
        fun returnsOcrResult() {
            given(imageCommandService.processWithMistral(member, "https://example.com/image.png"))
                .willReturn("text")

            mockMvc.perform(
                post("/api/v1/images/ocr")
                    .param("imageUrl", "https://example.com/image.png")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("text"))
        }
    }

    @Nested
    @DisplayName("GET /api/v0/images")
    inner class GetImageFiles {

        @Test
        @DisplayName("업로드 이미지 목록을 반환한다")
        fun returnsUploadedImages() {
            val response = PageResponse.of(
                listOf("https://example.com/image.png"),
                0,
                5,
                1,
                1
            )
            given(imageQueryService.fetchUploadedImages(1L, PageRequest.of(0, 5)))
                .willReturn(response)

            mockMvc.perform(
                get("/api/v0/images")
                    .param("page", "1")
                    .param("size", "5")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.page").value(1))
                .andExpect(jsonPath("$.result.content[0]").value("https://example.com/image.png"))

            val pageableCaptor = ArgumentCaptor.forClass(Pageable::class.java)
            verify(imageQueryService).fetchUploadedImages(eq(1L), pageableCaptor.capture())
            val pageable = pageableCaptor.value
            val request = PageRequest.of(pageable.pageNumber, pageable.pageSize)
            assertThat(request).isEqualTo(PageRequest.of(0, 5))
        }
    }
}
