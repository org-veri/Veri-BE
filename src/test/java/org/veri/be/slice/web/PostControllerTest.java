package org.veri.be.slice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.api.social.PostController;
import org.veri.be.domain.book.dto.book.BookResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.controller.enums.PostSortType;
import org.veri.be.domain.post.dto.request.PostCreateRequest;
import org.veri.be.domain.post.dto.response.LikeInfoResponse;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.domain.post.dto.response.PostFeedResponseItem;
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;
import org.veri.be.domain.post.service.PostCommandService;
import org.veri.be.domain.post.service.PostQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @Mock
    PostCommandService postCommandService;

    @Mock
    PostQueryService postQueryService;

    Member member;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        member = Member.builder()
                .id(1L)
                .email("member@test.com")
                .nickname("member")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
        MemberContext.setCurrentMember(member);

        PostController controller = new PostController(postCommandService, postQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiResponseAdvice())
                .setCustomArgumentResolvers(new AuthenticatedMemberResolver(new ThreadLocalCurrentMemberAccessor(null)))
                .build();
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Nested
    @DisplayName("POST /api/v1/posts")
    class CreatePost {

        @Test
        @DisplayName("게시글을 작성하면 ID를 반환한다")
        void returnsCreatedId() throws Exception {
            PostCreateRequest request = new PostCreateRequest(
                    "title",
                    "content",
                    List.of("https://example.com/image.png"),
                    10L
            );
            given(postCommandService.createPost(any(PostCreateRequest.class), eq(member))).willReturn(99L);

            mockMvc.perform(post("/api/v1/posts")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value(99L));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/my")
    class GetMyPosts {

        @Test
        @DisplayName("내 게시글 목록을 반환한다")
        void returnsMyPosts() throws Exception {
            PostFeedResponseItem item = new PostFeedResponseItem(
                    10L,
                    "title",
                    "content",
                    "https://example.com/thumb.png",
                    new MemberProfileResponse(1L, "member", "https://example.com/profile.png"),
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
            );
            given(postQueryService.getPostsOfMember(1L)).willReturn(List.of(item));

            mockMvc.perform(get("/api/v1/posts/my"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.count").value(1))
                    .andExpect(jsonPath("$.result.posts[0].postId").value(10L));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts")
    class GetPosts {

        @Test
        @DisplayName("페이지와 정렬 기준에 맞는 목록을 반환한다")
        void returnsFeed() throws Exception {
            Book book = Book.builder()
                    .id(3L)
                    .title("book")
                    .author("author")
                    .image("https://example.com/book.png")
                    .publisher("publisher")
                    .isbn("isbn-1")
                    .build();
            PostFeedQueryResult item = new PostFeedQueryResult(
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
            );
            PageImpl<PostFeedQueryResult> page = new PageImpl<>(
                    List.of(item),
                    PageRequest.of(0, 10),
                    1
            );
            given(postQueryService.getPostFeeds(0, 10, PostSortType.NEWEST)).willReturn(page);

            mockMvc.perform(get("/api/v1/posts")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sort", "newest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.posts[0].postId").value(20L));

            verify(postQueryService).getPostFeeds(0, 10, PostSortType.NEWEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/posts/{postId}")
    class GetPostDetail {

        @Test
        @DisplayName("게시글 상세를 반환한다")
        void returnsDetail() throws Exception {
            PostDetailResponse response = PostDetailResponse.builder()
                    .postId(30L)
                    .title("title")
                    .content("content")
                    .images(List.of("https://example.com/image.png"))
                    .author(new MemberProfileResponse(1L, "member", "https://example.com/profile.png"))
                    .book(BookResponse.builder().title("book").build())
                    .likeCount(2L)
                    .isLiked(true)
                    .likedMembers(List.of())
                    .comments(List.of())
                    .commentCount(1L)
                    .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                    .build();
            given(postQueryService.getPostDetail(30L, member)).willReturn(response);

            mockMvc.perform(get("/api/v1/posts/30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.postId").value(30L))
                    .andExpect(jsonPath("$.result.title").value("title"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/posts/{postId}")
    class DeletePost {

        @Test
        @DisplayName("게시글을 삭제하면 상태 코드 204를 반환한다")
        void deletesPost() throws Exception {
            mockMvc.perform(delete("/api/v1/posts/40"))
                    .andExpect(status().isNoContent());

            verify(postCommandService).deletePost(40L, member);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/{postId}/publish")
    class PublishPost {

        @Test
        @DisplayName("게시글을 공개하면 상태 코드 204를 반환한다")
        void publishesPost() throws Exception {
            mockMvc.perform(post("/api/v1/posts/50/publish"))
                    .andExpect(status().isNoContent());

            verify(postCommandService).publishPost(50L, member);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/{postId}/unpublish")
    class UnpublishPost {

        @Test
        @DisplayName("게시글을 비공개하면 상태 코드 204를 반환한다")
        void unpublishesPost() throws Exception {
            mockMvc.perform(post("/api/v1/posts/60/unpublish"))
                    .andExpect(status().isNoContent());

            verify(postCommandService).unPublishPost(60L, member);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/like/{postId}")
    class LikePost {

        @Test
        @DisplayName("좋아요를 추가하면 정보를 반환한다")
        void likesPost() throws Exception {
            given(postCommandService.likePost(70L, member)).willReturn(new LikeInfoResponse(5L, true));

            mockMvc.perform(post("/api/v1/posts/like/70"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.likeCount").value(5L))
                    .andExpect(jsonPath("$.result.isLiked").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/unlike/{postId}")
    class UnlikePost {

        @Test
        @DisplayName("좋아요를 취소하면 정보를 반환한다")
        void unlikesPost() throws Exception {
            given(postCommandService.unlikePost(80L, member)).willReturn(new LikeInfoResponse(4L, false));

            mockMvc.perform(post("/api/v1/posts/unlike/80"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.likeCount").value(4L))
                    .andExpect(jsonPath("$.result.isLiked").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/posts/image")
    class UploadImage {

        @Test
        @DisplayName("이미지 presigned URL을 반환한다")
        void returnsPresignedUrl() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 100L);
            PresignedUrlResponse response = new PresignedUrlResponse(
                    "https://example.com/presigned",
                    "https://example.com/public"
            );
            given(postCommandService.getPresignedUrl(any(PresignedUrlRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/posts/image")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.publicUrl").value("https://example.com/public"));
        }
    }
}
