package org.veri.be.slice.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.common.ImageController
import org.veri.be.domain.image.service.ImageCommandService
import org.veri.be.domain.image.service.ImageQueryService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.global.response.PageResponse
import org.veri.be.lib.response.ApiResponseAdvice
import org.veri.be.support.ControllerTestSupport
import org.veri.be.support.fixture.MemberFixture
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ImageControllerTest : ControllerTestSupport() {

    @org.mockito.Mock
    private lateinit var imageCommandService: ImageCommandService

    @org.mockito.Mock
    private lateinit var imageQueryService: ImageQueryService

    private lateinit var member: Member
    private lateinit var memberInfo: CurrentMemberInfo

    @BeforeEach
    fun setUp() {
        member = MemberFixture.aMember()
            .id(1L)
            .providerType(ProviderType.KAKAO)
            .build()

        memberInfo = CurrentMemberInfo.from(JwtClaimsPayload(member.id, member.email, member.nickname, false))
        val controller = ImageController(imageCommandService, imageQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(testMemberAccessor(memberInfo))
            )
            .build()
    }

    private fun testMemberAccessor(memberInfo: CurrentMemberInfo): CurrentMemberAccessor {
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfoOrNull() = memberInfo
            override fun getCurrentMember() = Optional.empty<Member>()
        }
    }

    @Nested
    @DisplayName("POST /api/v0/images/ocr")
    inner class OcrImageV0 {

        @Test
        @DisplayName("이미지를 OCR하면 → 결과를 반환한다")
        fun returnsOcrResult() {
            given(imageCommandService.processWithMistral(member.id, "https://example.com/image.png"))
                .willReturn("text")

            mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v0/images/ocr")
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
        @DisplayName("이미지를 OCR하면 → 결과를 반환한다")
        fun returnsOcrResult() {
            given(imageCommandService.processWithMistral(member.id, "https://example.com/image.png"))
                .willReturn("text")

            mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/images/ocr")
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
        @DisplayName("요청하면 → 업로드 이미지 목록을 반환한다")
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

            get(
                "/api/v0/images",
                mapOf(
                    "page" to "1",
                    "size" to "5"
                )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.page").value(1))
                .andExpect(jsonPath("$.result.content[0]").value("https://example.com/image.png"))

            val pageableCaptor = ArgumentCaptor.forClass(Pageable::class.java)
            then(imageQueryService).should().fetchUploadedImages(eq(1L), pageableCaptor.capture())
            val pageable = pageableCaptor.value
            val request = PageRequest.of(pageable.pageNumber, pageable.pageSize)
            assertThat(request).isEqualTo(PageRequest.of(0, 5))
        }
    }
}
