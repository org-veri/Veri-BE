package org.veri.be.slice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.common.dto.MemberProfileResponse
import org.veri.be.api.social.PostController
import org.veri.be.domain.book.dto.book.BookResponse
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.post.controller.enums.PostSortType
import org.veri.be.domain.post.dto.request.PostCreateRequest
import org.veri.be.domain.post.dto.response.LikeInfoResponse
import org.veri.be.domain.post.dto.response.PostDetailResponse
import org.veri.be.domain.post.dto.response.PostFeedResponseItem
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult
import org.veri.be.domain.post.service.PostCommandService
import org.veri.be.domain.post.service.PostQueryService
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.lib.response.ApiResponseAdvice
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PostControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var postCommandService: PostCommandService

    @org.mockito.Mock
    private lateinit var postQueryService: PostQueryService

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        member = Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()

        val controller = PostController(postCommandService, postQueryService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(testMemberAccessor(member))
            )
            .build()
    }

    private fun testMemberAccessor(member: Member): CurrentMemberAccessor {
        val info = CurrentMemberInfo.from(member)
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfo() = Optional.of(info)
            override fun getCurrentMember() = Optional.of(member)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts")
    inner class CreatePost {

        @Test
        @DisplayName("게시글을 작성하면 ID를 반환한다")
        fun returnsCreatedId() {
            val request = PostCreateRequest(
                "title",
                "content",
                listOf("https://example.com/image.png"),
                10L
            )
            given(postCommandService.createPost(any(PostCreateRequest::class.java), eq(member))).willReturn(99L)

            mockMvc.perform(
                post("/api/v1/posts")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").value(99L))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = PostCreateRequest(null, null, null, null)

            mockMvc.perform(
                post("/api/v1/posts")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/my")
    inner class GetMyPosts {

        @Test
        @DisplayName("내 게시글 목록을 반환한다")
        fun returnsMyPosts() {
            val item = PostFeedResponseItem(
                10L,
                "title",
                "content",
                "https://example.com/thumb.png",
                MemberProfileResponse(1L, "member", "https://example.com/profile.png"),
                BookResponse.builder()
                    .title("book")
                    .author("author")
                    .imageUrl("https://example.com/book.png")
                    .publisher("publisher")
                    .isbn("isbn-1")
                    .build(),
                2L,
                1L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                true
            )
            given(postQueryService.getPostsOfMember(1L)).willReturn(listOf(item))

            mockMvc.perform(get("/api/v1/posts/my"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.count").value(1))
                .andExpect(jsonPath("$.result.posts[0].postId").value(10L))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    inner class GetPosts {

        @Test
        @DisplayName("페이지와 정렬 기준에 맞는 목록을 반환한다")
        fun returnsFeed() {
            val book = Book.builder()
                .id(3L)
                .title("book")
                .author("author")
                .image("https://example.com/book.png")
                .publisher("publisher")
                .isbn("isbn-1")
                .build()
            val item = PostFeedQueryResult(
                20L,
                "title",
                "content",
                "https://example.com/thumb.png",
                member,
                book,
                1L,
                2L,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                true
            )
            val page = PageImpl(
                listOf(item),
                PageRequest.of(0, 10),
                1
            )
            given(postQueryService.getPostFeeds(0, 10, PostSortType.NEWEST)).willReturn(page)

            mockMvc.perform(
                get("/api/v1/posts")
                    .param("page", "1")
                    .param("size", "10")
                    .param("sort", "newest")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.posts[0].postId").value(20L))

            verify(postQueryService).getPostFeeds(0, 10, PostSortType.NEWEST)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}")
    inner class GetPostDetail {

        @Test
        @DisplayName("게시글 상세를 반환한다")
        fun returnsDetail() {
            val response = PostDetailResponse.builder()
                .postId(30L)
                .title("title")
                .content("content")
                .images(listOf("https://example.com/image.png"))
                .author(MemberProfileResponse(1L, "member", "https://example.com/profile.png"))
                .book(BookResponse.builder().title("book").build())
                .likeCount(2L)
                .isLiked(true)
                .likedMembers(listOf())
                .comments(listOf())
                .commentCount(1L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .build()
            given(postQueryService.getPostDetail(30L, member)).willReturn(response)

            mockMvc.perform(get("/api/v1/posts/30"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.postId").value(30L))
                .andExpect(jsonPath("$.result.title").value("title"))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/posts/{postId}")
    inner class DeletePost {

        @Test
        @DisplayName("게시글을 삭제하면 상태 코드 204를 반환한다")
        fun deletesPost() {
            mockMvc.perform(delete("/api/v1/posts/40"))
                .andExpect(status().isNoContent)

            verify(postCommandService).deletePost(40L, member)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/{postId}/publish")
    inner class PublishPost {

        @Test
        @DisplayName("게시글을 공개하면 상태 코드 204를 반환한다")
        fun publishesPost() {
            mockMvc.perform(post("/api/v1/posts/50/publish"))
                .andExpect(status().isNoContent)

            verify(postCommandService).publishPost(50L, member)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/{postId}/unpublish")
    inner class UnpublishPost {

        @Test
        @DisplayName("게시글을 비공개하면 상태 코드 204를 반환한다")
        fun unpublishesPost() {
            mockMvc.perform(post("/api/v1/posts/60/unpublish"))
                .andExpect(status().isNoContent)

            verify(postCommandService).unPublishPost(60L, member)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/like/{postId}")
    inner class LikePost {

        @Test
        @DisplayName("좋아요를 추가하면 정보를 반환한다")
        fun likesPost() {
            given(postCommandService.likePost(70L, member)).willReturn(LikeInfoResponse(5L, true))

            mockMvc.perform(post("/api/v1/posts/like/70"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.likeCount").value(5L))
                .andExpect(jsonPath("$.result.isLiked").value(true))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/unlike/{postId}")
    inner class UnlikePost {

        @Test
        @DisplayName("좋아요를 취소하면 정보를 반환한다")
        fun unlikesPost() {
            given(postCommandService.unlikePost(80L, member)).willReturn(LikeInfoResponse(4L, false))

            mockMvc.perform(post("/api/v1/posts/unlike/80"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.likeCount").value(4L))
                .andExpect(jsonPath("$.result.isLiked").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/image")
    inner class UploadImage {

        @Test
        @DisplayName("이미지 presigned URL을 반환한다")
        fun returnsPresignedUrl() {
            val request = PresignedUrlRequest("image/png", 100L)
            val response = PresignedUrlResponse(
                "https://example.com/presigned",
                "https://example.com/public"
            )
            given(postCommandService.getPresignedUrl(any(PresignedUrlRequest::class.java))).willReturn(response)

            mockMvc.perform(
                post("/api/v1/posts/image")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.publicUrl").value("https://example.com/public"))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = PresignedUrlRequest(null, 0)

            mockMvc.perform(
                post("/api/v1/posts/image")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }
}
