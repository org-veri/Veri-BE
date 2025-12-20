package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.comment.dto.request.CommentPostRequest;
import org.veri.be.domain.post.dto.request.PostCreateRequest;
import org.veri.be.domain.post.service.PostCommandService;
import org.veri.be.integration.IntegrationTestSupport;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentIntegrationTest extends IntegrationTestSupport {

    @Autowired BookRepository bookRepository;
    @Autowired PostCommandService postCommandService;

    @Nested
    @DisplayName("POST /api/v1/comments")
    class PostComment {
        @Test
        @DisplayName("댓글 작성 성공")
        void postCommentSuccess() throws Exception {
            Book book = Book.builder().title("T").image("I").isbn("ISBN").build();
            book = bookRepository.save(book);
            Long postId = postCommandService.createPost(
                    new PostCreateRequest("Post", "Content", List.of(), book.getId()),
                    getMockMember()
            );

            CommentPostRequest request = new CommentPostRequest(postId, "Comment content");

            mockMvc.perform(post("/api/v1/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").exists());
        }
    }
}
