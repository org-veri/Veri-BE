package org.veri.be.unit.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.veri.be.domain.image.client.MistralOcrClient;
import org.veri.be.domain.image.client.MistralOcrClientException;

@ExtendWith(MockitoExtension.class)
class MistralOcrClientTest {

    @Mock
    RestClient.Builder restClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    RestClient restClient;

    MistralOcrClient client;

    Class<?> responseClass;
    Class<?> pageClass;

    @BeforeEach
    void setUp() throws Exception {
        given(restClientBuilder.baseUrl("https://mistral.test")).willReturn(restClientBuilder);
        given(restClientBuilder.build()).willReturn(restClient);
        client = new MistralOcrClient(restClientBuilder, "https://mistral.test", "api-key", "ocr-model");
        responseClass = Class.forName("org.veri.be.domain.image.client.MistralOcrClient$MistralOcrResponse");
        pageClass = Class.forName("org.veri.be.domain.image.client.MistralOcrClient$Page");
    }

    @Nested
    @DisplayName("requestOcr")
    class RequestOcr {

        @Test
        @DisplayName("응답 페이지를 합쳐서 반환한다")
        void returnsMergedMarkdown() throws Exception {
            Object response = buildResponse(List.of("hello", "world"));
            ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
            given(restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(bodyCaptor.capture())
                    .retrieve()
                    .body(any(Class.class)))
                    .willAnswer(invocation -> response);

            String result = client.requestOcr("https://example.com/ocr/image.png");

            assertThat(result).isEqualTo("hello\nworld");
            Object requestBody = bodyCaptor.getValue();
            assertThat(ReflectionTestUtils.getField(requestBody, "model")).isEqualTo("ocr-model");
            Object document = ReflectionTestUtils.getField(requestBody, "document");
            Object imageUrl = ReflectionTestUtils.getField(document, "imageUrl");
            assertThat(ReflectionTestUtils.getField(imageUrl, "url"))
                    .isEqualTo("https://example.com/ocr/image.png");
        }

        @Test
        @DisplayName("응답이 비어있으면 예외가 발생한다")
        void throwsWhenResponseEmpty() {
            org.mockito.Mockito.lenient().when(restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(any())
                    .retrieve()
                    .body(any(Class.class)))
                    .thenReturn(null);

            assertThatThrownBy(() -> client.requestOcr("https://example.com/ocr/image.png"))
                    .isInstanceOf(MistralOcrClientException.class);
        }

        @Test
        @DisplayName("페이지가 비어있으면 예외가 발생한다")
        void throwsWhenPagesEmpty() throws Exception {
            Object response = buildResponse(List.of());
            org.mockito.Mockito.lenient().when(restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(any())
                    .retrieve()
                    .body(any(Class.class)))
                    .thenReturn(response);

            assertThatThrownBy(() -> client.requestOcr("https://example.com/ocr/image.png"))
                    .isInstanceOf(MistralOcrClientException.class);
        }

        @Test
        @DisplayName("호출 실패 시 예외가 발생한다")
        void throwsWhenRestClientFails() {
            org.mockito.Mockito.lenient().when(restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(any())
                    .retrieve()
                    .body(eq(responseClass)))
                    .thenThrow(new RestClientException("fail"));

            assertThatThrownBy(() -> client.requestOcr("https://example.com/ocr/image.png"))
                    .isInstanceOf(MistralOcrClientException.class);
        }

        @Test
        @DisplayName("요청 생성 단계에서 실패하면 예외가 발생한다")
        void throwsWhenPostFails() {
            org.mockito.Mockito.lenient().when(restClient.post())
                    .thenThrow(new RestClientException("fail"));

            assertThatThrownBy(() -> client.requestOcr("https://example.com/ocr/image.png"))
                    .isInstanceOf(MistralOcrClientException.class);
        }
    }

    private Object buildResponse(List<String> pages) throws Exception {
        Object response = newInstance(responseClass);
        List<Object> pageResults = new ArrayList<>();
        for (String pageText : pages) {
            Object page = newInstance(pageClass);
            ReflectionTestUtils.setField(page, "markdown", pageText);
            pageResults.add(page);
        }
        ReflectionTestUtils.setField(response, "pages", pageResults);
        return response;
    }

    private Object newInstance(Class<?> targetClass) throws Exception {
        Constructor<?> constructor = targetClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
