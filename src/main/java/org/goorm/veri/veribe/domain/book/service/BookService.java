package org.goorm.veri.veribe.domain.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.config.NaverConfig;
import org.goorm.veri.veribe.domain.book.dto.book.BookConverter;
import org.goorm.veri.veribe.domain.book.dto.book.BookSearchResponse;
import org.goorm.veri.veribe.domain.book.dto.book.NaverBookResponse;
import org.goorm.veri.veribe.domain.book.entity.Book;
import org.goorm.veri.veribe.domain.book.exception.BookErrorInfo;
import org.goorm.veri.veribe.domain.book.repository.BookRepository;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;
import org.goorm.veri.veribe.global.exception.http.NotFoundException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;
    private final NaverConfig naverConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public BookSearchResponse searchBook(String query, int page, int size) {
        int start = (page - 1) * size + 1;
        if (start > 1000) {
            throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com")
                .path("/v1/search/book.json")
                .queryParam("query", query)
                .queryParam("display", size)
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
            throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        }

        String body = respEntity.getBody();
        try {
            NaverBookResponse naverResp = objectMapper.readValue(body, NaverBookResponse.class);
            return BookConverter.toBookSearchResponse(naverResp);
        } catch (JsonProcessingException e) {
            throw new BadRequestException(BookErrorInfo.BAD_REQUEST);
        }
    }

    public Book getBookById(Long bookId) {
        if (bookId == null) return null;
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND));
    }
}
