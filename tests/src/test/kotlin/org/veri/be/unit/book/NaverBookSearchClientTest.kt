package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.veri.be.book.client.NaverBookSearchClient
import org.veri.be.book.client.NaverClientException
import org.veri.be.book.config.NaverConfig
import org.veri.be.book.dto.book.NaverBookResponse
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.util.function.Consumer

@ExtendWith(MockitoExtension::class)
class NaverBookSearchClientTest {

    @org.mockito.Mock
    private lateinit var restClient: RestClient

    @org.mockito.Mock
    private lateinit var requestSpec: RestClient.RequestHeadersUriSpec<*>

    @org.mockito.Mock
    private lateinit var headersSpec: RestClient.RequestHeadersSpec<*>

    @org.mockito.Mock
    private lateinit var responseSpec: RestClient.ResponseSpec

    private lateinit var naverConfig: NaverConfig

    private val objectMapper = ObjectMapper()

    private lateinit var client: NaverBookSearchClient

    @BeforeEach
    fun setUp() {
        naverConfig = NaverConfig()
        ReflectionTestUtils.setField(naverConfig, "clientId", "client-id")
        ReflectionTestUtils.setField(naverConfig, "clientSecret", "client-secret")
        client = NaverBookSearchClient(restClient, naverConfig, objectMapper)
        lenient().`when`(restClient.get()).thenReturn(requestSpec)
        lenient().`when`(requestSpec.uri(any(URI::class.java))).thenReturn(headersSpec)
        lenient().`when`(headersSpec.headers(any<Consumer<HttpHeaders>>())).thenReturn(headersSpec)
        lenient().`when`(headersSpec.retrieve()).thenReturn(responseSpec)
    }

    @Nested
    @DisplayName("search")
    inner class Search {

        @Test
        @DisplayName("요청 파라미터와 헤더를 포함해 검색한다")
        fun callsNaverSearch() {
            val body = "{\"total\":1,\"start\":1,\"display\":10,\"items\":[]}";
            given(responseSpec.toEntity(eq(String::class.java)))
                .willReturn(ResponseEntity(body, HttpStatus.OK))

            val response: NaverBookResponse = client.search("query", 1, 10)

            assertThat(response.total).isEqualTo(1)
            verifyRequestUri()
        }

        @Test
        @DisplayName("start가 1000을 초과하면 예외가 발생한다")
        fun throwsWhenStartTooLarge() {
            assertThatThrownBy { client.search("query", 101, 10) }
                .isInstanceOf(NaverClientException::class.java)
        }

        @Test
        @DisplayName("서버 오류 응답이면 예외가 발생한다")
        fun throwsWhenServerError() {
            given(responseSpec.toEntity(eq(String::class.java)))
                .willThrow(RestClientException("fail"))

            assertThatThrownBy { client.search("query", 1, 10) }
                .isInstanceOf(NaverClientException::class.java)
        }

        @Test
        @DisplayName("응답 파싱 실패 시 예외가 발생한다")
        fun throwsWhenParsingFails() {
            given(responseSpec.toEntity(eq(String::class.java)))
                .willReturn(ResponseEntity("invalid-json", HttpStatus.OK))

            assertThatThrownBy { client.search("query", 1, 10) }
                .isInstanceOf(NaverClientException::class.java)
        }

        private fun verifyRequestUri() {
            val uriCaptor = ArgumentCaptor.forClass(URI::class.java)
            given(requestSpec.uri(uriCaptor.capture())).willReturn(headersSpec)
            given(responseSpec.toEntity(eq(String::class.java)))
                .willReturn(ResponseEntity("{\"total\":0,\"start\":1,\"display\":10,\"items\":[]}", HttpStatus.OK))

            client.search("query", 1, 10)

            val uri = uriCaptor.value
            assertThat(uri.toString()).contains("query=query")
            assertThat(uri.toString()).contains("display=10")
            assertThat(uri.toString()).contains("start=1")
        }
    }
}
