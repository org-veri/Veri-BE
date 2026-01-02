package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.post.dto.request.PostCreateRequest
import org.veri.be.domain.post.repository.PostRepository
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.PostFixture
import org.veri.be.support.steps.PostSteps

class PostIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Nested
    @DisplayName("POST /api/v1/posts")
    inner class CreatePost {
        @Test
        @DisplayName("게시글을 작성하면 → 201을 반환한다")
        fun createPostSuccess() {
            val book = createBook()

            val request = PostCreateRequest(
                "My Post",
                "Content",
                listOf("img1.png"),
                book.id
            )

            PostSteps.requestCreatePost(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").exists())
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/my")
    inner class GetMyPosts {
        @Test
        @DisplayName("나의 게시글 목록을 조회하면 → 결과를 반환한다")
        fun getMyPostsSuccess() {
            createPost(getMockMember().id, true)

            PostSteps.getMyPosts(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.count").value(1))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    inner class GetFeed {
        @Test
        @DisplayName("전체 feed를 조회하면 → 최신순으로 반환한다")
        fun getFeedSuccess() {
            createPost(getMockMember().id, true)

            PostSteps.getFeed(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.posts[0]").exists())
        }

        @Test
        @DisplayName("page=0이면 → 400을 반환한다")
        fun invalidPage() {
            PostSteps.getFeed(mockMvc, mapOf("page" to "0"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("정렬 파라미터 오류면 → 400을 반환한다")
        fun invalidSort() {
            PostSteps.getFeed(mockMvc, mapOf("sort" to "INVALID"))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}")
    inner class GetDetail {
        @Test
        @DisplayName("상세 조회하면 → 200을 반환한다")
        fun getDetailSuccess() {
            val post = createPost(getMockMember().id, true)

            PostSteps.getDetail(mockMvc, post.id)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.postId").value(post.id))
        }

        @Test
        @DisplayName("존재하지 않는 게시글이면 → 404를 반환한다")
        fun getDetailNotFound() {
            PostSteps.getDetail(mockMvc, 999L)
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/image")
    inner class PresignedUrl {
        @Test
        @DisplayName("presigned URL을 발급하면 → 200을 반환한다")
        fun urlSuccess() {
            val request = PresignedUrlRequest("image/png", 1000L)

            PostSteps.requestPresignedUrl(mockMvc, objectMapper, request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.presignedUrl").value("http://stub.presigned.url"))
        }

        @Test
        @DisplayName("용량이 초과되면 → 400을 반환한다")
        fun urlTooLarge() {
            val request = PresignedUrlRequest("image/png", 10 * 1024 * 1024L)

            PostSteps.requestPresignedUrl(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("이미지 타입이 아니면 → 400을 반환한다")
        fun urlInvalidType() {
            val request = PresignedUrlRequest("text/plain", 1000L)

            PostSteps.requestPresignedUrl(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }
    }

    private fun createBook(): org.veri.be.domain.book.entity.Book {
        val book = BookFixture.aBook()
            .title("Title")
            .image("Img")
            .author("Author")
            .publisher("Pub")
            .isbn("ISBN")
            .build()
        return bookRepository.save(book)
    }

    private fun createPost(memberId: Long, isPublic: Boolean): org.veri.be.domain.post.entity.Post {
        val book = createBook()
        val post = PostFixture.aPost()
            .author(memberRepository.findById(memberId).orElseThrow())
            .book(book)
            .title("Post")
            .content("Content")
            .isPublic(isPublic)
            .build()
        return postRepository.save(post)
    }
}
