package org.veri.be.slice.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.personal.CardControllerV2;
import org.veri.be.domain.card.service.CardCommandService;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class CardControllerV2Test {

    MockMvc mockMvc;

    @Mock
    CardCommandService cardCommandService;

    @BeforeEach
    void setUp() {
        CardControllerV2 controller = new CardControllerV2(cardCommandService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiResponseAdvice())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v2/cards/image")
    class UploadCardImageV2 {

        @Test
        @DisplayName("presigned post form을 반환한다")
        void returnsPresignedPostForm() throws Exception {
            PresignedPostForm form = mock(PresignedPostForm.class);
            given(cardCommandService.getPresignedPost()).willReturn(form);

            mockMvc.perform(post("/api/v2/cards/image"))
                    .andExpect(status().isOk());

            verify(cardCommandService).getPresignedPost();
        }
    }
}
