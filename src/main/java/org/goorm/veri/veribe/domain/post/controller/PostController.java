package org.goorm.veri.veribe.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.controller.enums.PostSortType;
import org.goorm.veri.veribe.domain.post.dto.request.PostCreateRequest;
import org.goorm.veri.veribe.domain.post.dto.response.LikeInfoResponse;
import org.goorm.veri.veribe.domain.post.dto.response.PostDetailResponse;
import org.goorm.veri.veribe.domain.post.dto.response.PostFeedResponse;
import org.goorm.veri.veribe.domain.post.dto.response.PostListResponse;
import org.goorm.veri.veribe.domain.post.service.PostCommandService;
import org.goorm.veri.veribe.domain.post.service.PostQueryService;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시글 API")
@RequestMapping("/api/v1/posts")
@RestController
@RequiredArgsConstructor
public class PostController {

    public final PostCommandService postCommandService;
    public final PostQueryService postQueryService;


    @PostMapping
    public ApiResponse<Long> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.created(this.postCommandService.createPost(request, member));
    }

    @GetMapping("/my")
    public ApiResponse<PostListResponse> getMyPosts(@AuthenticatedMember Member member) {
        return ApiResponse.ok(PostListResponse.from(postQueryService.getPostsOfMember(member.getId())));
    }

    @Operation(summary = "전체 게시글 목록 조회")
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
    public ApiResponse<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ApiResponse.ok(this.postQueryService.getPostDetail(postId));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Long postId
    ) {
        this.postCommandService.deletePost(postId);
        return ApiResponse.noContent();
    }

    @PostMapping("/like/{postId}")
    public ApiResponse<LikeInfoResponse> likePost(
            @PathVariable Long postId,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.ok(this.postCommandService.likePost(postId, member));
    }

    @PostMapping("/unlike/{postId}")
    public ApiResponse<LikeInfoResponse> unlikePost(
            @PathVariable Long postId,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.ok(this.postCommandService.unlikePost(postId, member));
    }

    @Operation(summary = "이미지 presigned URL 발급")
    @PostMapping("/image")
    public ApiResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        return ApiResponse.ok(postCommandService.getPresignedUrl(request));
    }
}
