package org.goorm.veri.veribe.domain.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.entity.Book;
import org.goorm.veri.veribe.domain.book.config.NaverConfig;
import org.goorm.veri.veribe.domain.book.dto.book.BookConverter;
import org.goorm.veri.veribe.domain.book.dto.book.BookResponse;
import org.goorm.veri.veribe.domain.book.dto.book.NaverBookItem;
import org.goorm.veri.veribe.domain.book.dto.book.NaverBookResponse;
import org.goorm.veri.veribe.domain.book.exception.NaverAPIException;
import org.goorm.veri.veribe.domain.book.repository.BookRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.goorm.veri.veribe.domain.book.exception.NaverAPIErrorCode.BAD_REQUEST;

@Service
@Transactional
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;
    private final NaverConfig naverConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long addBook(String title, String image, String author, String publisher, String isbn) {

        Optional<Book> findBook = bookRepository.findBookByIsbn(isbn);
        if (findBook.isPresent()) {
            return findBook.get().getId();
        }

        Book book = Book.builder()
                .title(title)
                .image(image)
                .author(author)
                .publisher(publisher)
                .isbn(isbn)
                .build();

        bookRepository.save(book);
        return book.getId();
    }

    /**
     * Naver OpenAPI 활용해 책의 정보를 보여주는 메서드
     */
    @Override
    public List<BookResponse> searchBook(String query, int display, int start) {

        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/book.json")
                .queryParam("query", query)
                .queryParam("display", display)
                .queryParam("start", start)
                .queryParam("sort", "sim")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverConfig.getClientId());
        headers.set("X-Naver-Client-Secret", naverConfig.getClientSecret());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> respEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        if (respEntity.getStatusCode().is5xxServerError() || respEntity.getBody() == null) {
            // 네이버 API 호출 실패 시 빈 리스트 또는 오류 응답 처리
            throw new NaverAPIException(BAD_REQUEST);
        }

        String body = respEntity.getBody();
        try {
            NaverBookResponse naverResp = objectMapper.readValue(body, NaverBookResponse.class);
            List<NaverBookItem> items = naverResp.getItems();
            List<BookResponse> result = new ArrayList<>();
            for (NaverBookItem item : items) {
                result.add(BookConverter.toBookResponse(item));
            }

            return result;
        } catch (JsonProcessingException e) {
            throw new NaverAPIException(BAD_REQUEST);
        }
    }
}
