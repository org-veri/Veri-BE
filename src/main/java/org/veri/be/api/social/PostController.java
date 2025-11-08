package org.veri.be.api.social;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.veri.be.domain.auth.annotation.AuthenticatedMember;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.post.controller.enums.PostSortType;
import org.veri.be.domain.post.dto.request.PostCreateRequest;
import org.veri.be.domain.post.dto.response.LikeInfoResponse;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.domain.post.dto.response.PostFeedResponse;
import org.veri.be.domain.post.dto.response.PostListResponse;
import org.veri.be.domain.post.service.PostCommandService;
import org.veri.be.domain.post.service.PostQueryService;
import org.veri.be.lib.response.ApiResponse;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "소셜")
@Tag(name = "게시글")
@RequestMapping("/api/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {

    public final PostCommandService postCommandService;
    public final PostQueryService postQueryService;


    @PostMapping
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    public ApiResponse<Long> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.created(this.postCommandService.createPost(request, member));
    }

    @GetMapping("/my")
    @Operation(summary = "내 게시글 목록 조회", description = "로그인한 사용자의 게시글 목록을 조회합니다.")
    public ApiResponse<PostListResponse> getMyPosts(@AuthenticatedMember Member member) {
        return ApiResponse.ok(PostListResponse.from(postQueryService.getPostsOfMember(member.getId())));
    }

    @Operation(summary = "전체 게시글 목록 조회", description = "모든 사용자의 게시글 목록을 페이지네이션과 정렬 기준으로 조회합니다.")
    @GetMapping()
    public ApiResponse<PostFeedResponse> getCards(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        PostSortType sortType = PostSortType.from(sort);
        return ApiResponse.ok(
                new PostFeedResponse(postQueryService.getPostFeeds(page - 1, size, sortType))
        );
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 게시글의 상세 정보를 조회합니다.")
    public ApiResponse<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ApiResponse.ok(this.postQueryService.getPostDetail(postId));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId
    ) {
        this.postCommandService.deletePost(postId);
        return ApiResponse.noContent();
    }

    @PostMapping("/{postId}/publish")
    @Operation(summary = "게시글 공개", description = "게시글을 공개합니다.")
    public ApiResponse<Void> publishPost(
            @PathVariable Long postId
    ) {
        this.postCommandService.publishPost(postId);
        return ApiResponse.noContent();
    }

    @PostMapping("/{postId}/unpublish")
    @Operation(summary = "게시글 비공개", description = "게시글을 비공개합니다.")
    public ApiResponse<Void> unPublishPost(
            @PathVariable Long postId
    ) {
        this.postCommandService.unPublishPost(postId);
        return ApiResponse.noContent();
    }

    @PostMapping("/like/{postId}")
    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 추가합니다.")
    public ApiResponse<LikeInfoResponse> likePost(
            @PathVariable Long postId,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.ok(this.postCommandService.likePost(postId, member));
    }

    @PostMapping("/unlike/{postId}")
    @Operation(summary = "게시글 좋아요 취소", description = "게시글의 좋아요를 취소합니다.")
    public ApiResponse<LikeInfoResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.ok(this.postCommandService.unlikePost(postId, member));
    }

    @Operation(summary = "게시글 이미지 presigned URL 발급", description = "게시글 이미지 업로드를 위한 presigned URL을 발급합니다.")
    @PostMapping("/image")
    public ApiResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        return ApiResponse.ok(postCommandService.getPresignedUrl(request));
    }
}
