package org.veri.be.unit.image

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.veri.be.domain.image.client.MistralOcrClient
import org.veri.be.domain.image.client.MistralOcrClientException
import java.lang.reflect.Constructor

@ExtendWith(MockitoExtension::class)
class MistralOcrClientTest {

    @org.mockito.Mock
    private lateinit var restClientBuilder: RestClient.Builder

    @org.mockito.Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var restClient: RestClient

    private lateinit var client: MistralOcrClient

    private lateinit var responseClass: Class<*>
    private lateinit var pageClass: Class<*>

    @BeforeEach
    fun setUp() {
        given(restClientBuilder.baseUrl("https://mistral.test")).willReturn(restClientBuilder)
        given(restClientBuilder.build()).willReturn(restClient)
        client = MistralOcrClient(restClientBuilder, "https://mistral.test", "api-key", "ocr-model")
        responseClass = Class.forName("org.veri.be.domain.image.client.MistralOcrClient\$MistralOcrResponse")
        pageClass = Class.forName("org.veri.be.domain.image.client.MistralOcrClient\$Page")
    }

    @Nested
    @DisplayName("requestOcr")
    inner class RequestOcr {

        @Test
        @DisplayName("응답 페이지가 있으면 → 병합 결과를 반환한다")
        fun returnsMergedMarkdown() {
            val response = buildResponse(listOf("hello", "world"))
            val bodyCaptor = ArgumentCaptor.forClass(Any::class.java)
            given(
                restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(bodyCaptor.capture())
                    .retrieve()
                    .body(any(Class::class.java))
            ).willAnswer { response }

            val result = client.requestOcr("https://example.com/ocr/image.png")

            assertThat(result).isEqualTo("hello\nworld")
            val requestBody = bodyCaptor.value
            assertThat(ReflectionTestUtils.getField(requestBody, "model")).isEqualTo("ocr-model")
            val document = requireNotNull(ReflectionTestUtils.getField(requestBody, "document"))
            val imageUrl = requireNotNull(ReflectionTestUtils.getField(document, "imageUrl"))
            assertThat(ReflectionTestUtils.getField(imageUrl, "url"))
                .isEqualTo("https://example.com/ocr/image.png")
        }

        @Test
        @DisplayName("응답이 비어있으면 → 예외가 발생한다")
        fun throwsWhenResponseEmpty() {
            org.mockito.Mockito.lenient().`when`(
                restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(any())
                    .retrieve()
                    .body(any(Class::class.java))
            ).thenReturn(null)

            assertThatThrownBy { client.requestOcr("https://example.com/ocr/image.png") }
                .isInstanceOf(MistralOcrClientException::class.java)
        }

        @Test
        @DisplayName("페이지가 비어있으면 → 예외가 발생한다")
        fun throwsWhenPagesEmpty() {
            val response = buildResponse(listOf())
            org.mockito.Mockito.lenient().`when`(
                restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(any())
                    .retrieve()
                    .body(any(Class::class.java))
            ).thenReturn(response)

            assertThatThrownBy { client.requestOcr("https://example.com/ocr/image.png") }
                .isInstanceOf(MistralOcrClientException::class.java)
        }

        @Test
        @DisplayName("호출에 실패하면 → 예외가 발생한다")
        fun throwsWhenRestClientFails() {
            org.mockito.Mockito.lenient().`when`(
                restClient.post()
                    .header(eq(HttpHeaders.AUTHORIZATION), eq("Bearer api-key"))
                    .contentType(eq(MediaType.APPLICATION_JSON))
                    .body(any())
                    .retrieve()
                    .body(eq(responseClass))
            ).thenThrow(RestClientException("fail"))

            assertThatThrownBy { client.requestOcr("https://example.com/ocr/image.png") }
                .isInstanceOf(MistralOcrClientException::class.java)
        }

        @Test
        @DisplayName("요청 생성에 실패하면 → 예외가 발생한다")
        fun throwsWhenPostFails() {
            org.mockito.Mockito.lenient().`when`(restClient.post())
                .thenThrow(RestClientException("fail"))

            assertThatThrownBy { client.requestOcr("https://example.com/ocr/image.png") }
                .isInstanceOf(MistralOcrClientException::class.java)
        }
    }

    private fun buildResponse(pages: List<String>): Any {
        val response = newInstance(responseClass)
        val pageResults = mutableListOf<Any>()
        for (pageText in pages) {
            val page = newInstance(pageClass)
            ReflectionTestUtils.setField(page, "markdown", pageText)
            pageResults.add(page)
        }
        ReflectionTestUtils.setField(response, "pages", pageResults)
        return response
    }

    private fun newInstance(targetClass: Class<*>): Any {
        val constructor: Constructor<*> = targetClass.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }
}
