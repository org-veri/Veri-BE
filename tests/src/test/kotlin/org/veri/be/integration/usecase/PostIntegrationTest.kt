package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.book.entity.Book
import org.veri.be.book.service.BookRepository
import org.veri.be.post.dto.request.PostCreateRequest
import org.veri.be.post.entity.Post
import org.veri.be.post.service.PostRepository
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.service.StorageService
import org.veri.be.integration.IntegrationTestSupport

class PostIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var storageService: StorageService

    @Nested
    @DisplayName("POST /api/v1/posts")
    inner class CreatePost {
        @Test
        @DisplayName("게시글 작성")
        fun createPostSuccess() {
            val book = createBook()

            val request = PostCreateRequest(
                "My Post",
                "Content",
                listOf("img1.png"),
                book.id
            )

            mockMvc.perform(
                post("/api/v1/posts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").exists())
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/my")
    inner class GetMyPosts {
        @Test
        @DisplayName("나의 게시글 목록")
        fun getMyPostsSuccess() {
            createPost(getMockMember().id, true)

            mockMvc.perform(get("/api/v1/posts/my"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.count").value(1))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    inner class GetFeed {
        @Test
        @DisplayName("전체 feed 최신순")
        fun getFeedSuccess() {
            createPost(getMockMember().id, true)

            mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.posts[0]").exists())
        }

        @Test
        @DisplayName("page=0")
        fun invalidPage() {
            mockMvc.perform(
                get("/api/v1/posts")
                    .param("page", "0")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("정렬 파라미터 오류")
        fun invalidSort() {
            mockMvc.perform(
                get("/api/v1/posts")
                    .param("sort", "INVALID")
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}")
    inner class GetDetail {
        @Test
        @DisplayName("상세 조회 성공")
        fun getDetailSuccess() {
            val post = createPost(getMockMember().id, true)

            mockMvc.perform(get("/api/v1/posts/${post.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.postId").value(post.id))
        }

        @Test
        @DisplayName("존재하지 않는 게시글")
        fun getDetailNotFound() {
            mockMvc.perform(get("/api/v1/posts/999"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/image")
    inner class PresignedUrl {
        @Test
        @DisplayName("presigned URL 발급")
        fun urlSuccess() {
            val request = PresignedUrlRequest("image/png", 1000L)

            mockMvc.perform(
                post("/api/v1/posts/image")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.presignedUrl").value("http://stub.presigned.url"))
        }

        @Test
        @DisplayName("용량 초과")
        fun urlTooLarge() {
            val request = PresignedUrlRequest("image/png", 10 * 1024 * 1024L)

            mockMvc.perform(
                post("/api/v1/posts/image")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("이미지 타입 아님")
        fun urlInvalidType() {
            val request = PresignedUrlRequest("text/plain", 1000L)

            mockMvc.perform(
                post("/api/v1/posts/image")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    private fun createBook(): Book {
        val book = Book.builder()
            .title("Title")
            .image("Img")
            .author("Author")
            .publisher("Pub")
            .isbn("ISBN")
            .build()
        return bookRepository.save(book)
    }

    private fun createPost(memberId: Long, isPublic: Boolean): Post {
        val book = createBook()
        val post = Post.builder()
            .author(memberRepository.findById(memberId).orElseThrow())
            .book(book)
            .title("Post")
            .content("Content")
            .isPublic(isPublic)
            .build()
        return postRepository.save(post)
    }
}
