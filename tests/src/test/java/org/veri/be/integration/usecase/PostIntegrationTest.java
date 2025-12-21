package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.post.dto.request.PostCreateRequest;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.integration.IntegrationTestSupport;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostIntegrationTest extends IntegrationTestSupport {

    @Autowired BookRepository bookRepository;
    @Autowired PostRepository postRepository;
    @Autowired StorageService storageService;

    @Nested
    @DisplayName("POST /api/v1/posts")
    class CreatePost {
        @Test
        @DisplayName("게시글 작성")
        void createPostSuccess() throws Exception {
            Book book = createBook();

            PostCreateRequest request = new PostCreateRequest(
                    "My Post",
                    "Content",
                    List.of("img1.png"),
                    book.getId()
            );

            mockMvc.perform(post("/api/v1/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/my")
    class GetMyPosts {
        @Test
        @DisplayName("나의 게시글 목록")
        void getMyPostsSuccess() throws Exception {
            createPost(getMockMember().getId(), true);

            mockMvc.perform(get("/api/v1/posts/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.count").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    class GetFeed {
        @Test
        @DisplayName("전체 feed 최신순")
        void getFeedSuccess() throws Exception {
            createPost(getMockMember().getId(), true);

            mockMvc.perform(get("/api/v1/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.posts[0]").exists());
        }

        @Test
        @DisplayName("page=0")
        void invalidPage() throws Exception {
            mockMvc.perform(get("/api/v1/posts")
                            .param("page", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("정렬 파라미터 오류")
        void invalidSort() throws Exception {
            mockMvc.perform(get("/api/v1/posts")
                            .param("sort", "INVALID"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}")
    class GetDetail {
        @Test
        @DisplayName("상세 조회 성공")
        void getDetailSuccess() throws Exception {
            Post post = createPost(getMockMember().getId(), true);

            mockMvc.perform(get("/api/v1/posts/" + post.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.postId").value(post.getId()));
        }

        @Test
        @DisplayName("존재하지 않는 게시글")
        void getDetailNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/posts/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/image")
    class PresignedUrl {
        @Test
        @DisplayName("presigned URL 발급")
        void urlSuccess() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 1000L);

            mockMvc.perform(post("/api/v1/posts/image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.presignedUrl").value("http://stub.presigned.url"));
        }

        @Test
        @DisplayName("용량 초과")
        void urlTooLarge() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 10 * 1024 * 1024L);

            mockMvc.perform(post("/api/v1/posts/image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이미지 타입 아님")
        void urlInvalidType() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("text/plain", 1000L);

            mockMvc.perform(post("/api/v1/posts/image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    private Book createBook() {
        Book book = Book.builder()
                .title("Title")
                .image("Img")
                .author("Author")
                .publisher("Pub")
                .isbn("ISBN")
                .build();
        return bookRepository.save(book);
    }

    private Post createPost(Long memberId, boolean isPublic) {
        Book book = createBook();
        Post post = Post.builder()
                .author(memberRepository.findById(memberId).orElseThrow())
                .book(book)
                .title("Post")
                .content("Content")
                .isPublic(isPublic)
                .build();
        return postRepository.save(post);
    }
}
