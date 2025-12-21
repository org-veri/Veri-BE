package org.veri.be.slice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.common.ImageController;
import org.veri.be.domain.image.service.ImageCommandService;
import org.veri.be.domain.image.service.ImageQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.global.response.PageResponse;
import org.veri.be.lib.response.ApiResponseAdvice;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @Mock
    ImageCommandService imageCommandService;

    @Mock
    ImageQueryService imageQueryService;

    Member member;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        member = Member.builder()
                .id(1L)
                .email("member@test.com")
                .nickname("member")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
        MemberContext.setCurrentMember(member);

        ImageController controller = new ImageController(imageCommandService, imageQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiResponseAdvice())
                .setCustomArgumentResolvers(new AuthenticatedMemberResolver(new ThreadLocalCurrentMemberAccessor(null)))
                .build();
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Nested
    @DisplayName("POST /api/v0/images/ocr")
    class OcrImageV0 {

        @Test
        @DisplayName("이미지 OCR 결과를 반환한다")
        void returnsOcrResult() throws Exception {
            given(imageCommandService.processWithMistral(member, "https://example.com/image.png"))
                    .willReturn("text");

            mockMvc.perform(post("/api/v0/images/ocr")
                            .param("imageUrl", "https://example.com/image.png"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("text"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/images/ocr")
    class OcrImageV1 {

        @Test
        @DisplayName("이미지 OCR 결과를 반환한다")
        void returnsOcrResult() throws Exception {
            given(imageCommandService.processWithMistral(member, "https://example.com/image.png"))
                    .willReturn("text");

            mockMvc.perform(post("/api/v1/images/ocr")
                            .param("imageUrl", "https://example.com/image.png"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("text"));
        }
    }

    @Nested
    @DisplayName("GET /api/v0/images")
    class GetImageFiles {

        @Test
        @DisplayName("업로드 이미지 목록을 반환한다")
        void returnsUploadedImages() throws Exception {
            PageResponse<List<String>> response = PageResponse.of(
                    List.of("https://example.com/image.png"),
                    0,
                    5,
                    1,
                    1
            );
            given(imageQueryService.fetchUploadedImages(1L, PageRequest.of(0, 5)))
                    .willReturn(response);

            mockMvc.perform(get("/api/v0/images")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.page").value(1))
                    .andExpect(jsonPath("$.result.content[0]").value("https://example.com/image.png"));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(imageQueryService).fetchUploadedImages(eq(1L), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            PageRequest request = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            org.assertj.core.api.Assertions.assertThat(request).isEqualTo(PageRequest.of(0, 5));
        }
    }
}
