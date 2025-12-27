package org.veri.be.unit.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.veri.be.domain.book.client.NaverBookSearchClient;
import org.veri.be.domain.book.client.NaverClientException;
import org.veri.be.domain.book.config.NaverConfig;
import org.veri.be.domain.book.dto.book.NaverBookResponse;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NaverBookSearchClientTest {

    @Mock
    RestTemplate restTemplate;

    NaverConfig naverConfig;

    ObjectMapper objectMapper = new ObjectMapper();

    NaverBookSearchClient client;

    @Captor
    ArgumentCaptor<URI> uriCaptor;

    @BeforeEach
    void setUp() {
        naverConfig = new NaverConfig();
        ReflectionTestUtils.setField(naverConfig, "clientId", "client-id");
        ReflectionTestUtils.setField(naverConfig, "clientSecret", "client-secret");
        client = new NaverBookSearchClient(restTemplate, naverConfig, objectMapper);
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("요청 파라미터와 헤더를 포함해 검색한다")
        void callsNaverSearch() throws Exception {
            String body = "{\"total\":1,\"start\":1,\"display\":10,\"items\":[]}";
            given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                    .willReturn(new ResponseEntity<>(body, HttpStatus.OK));

            NaverBookResponse response = client.search("query", 1, 10);

            assertThat(response.getTotal()).isEqualTo(1);
            verifyRequestUri();
        }

        @Test
        @DisplayName("start가 1000을 초과하면 예외가 발생한다")
        void throwsWhenStartTooLarge() {
            assertThatThrownBy(() -> client.search("query", 101, 10))
                    .isInstanceOf(NaverClientException.class);
        }

        @Test
        @DisplayName("서버 오류 응답이면 예외가 발생한다")
        void throwsWhenServerError() {
            given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                    .willReturn(new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR));

            assertThatThrownBy(() -> client.search("query", 1, 10))
                    .isInstanceOf(NaverClientException.class);
        }

        @Test
        @DisplayName("응답 파싱 실패 시 예외가 발생한다")
        void throwsWhenParsingFails() {
            given(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                    .willReturn(new ResponseEntity<>("invalid-json", HttpStatus.OK));

            assertThatThrownBy(() -> client.search("query", 1, 10))
                    .isInstanceOf(NaverClientException.class);
        }

        private void verifyRequestUri() {
            given(restTemplate.exchange(uriCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                    .willReturn(new ResponseEntity<>("{\"total\":0,\"start\":1,\"display\":10,\"items\":[]}", HttpStatus.OK));

            client.search("query", 1, 10);

            URI uri = uriCaptor.getValue();
            assertThat(uri.toString()).contains("query=query");
            assertThat(uri.toString()).contains("display=10");
            assertThat(uri.toString()).contains("start=1");
        }
    }
}
