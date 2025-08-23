package org.goorm.veri.veribe.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.dto.request.PostCreateRequest;
import org.goorm.veri.veribe.domain.post.dto.response.PostDetailResponse;
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
    public ApiResponse<Void> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticatedMember Member member
    ) {
        // Todo.
        return ApiResponse.created(null);
    }

    @GetMapping("/my")
    public ApiResponse<Void> getMyPosts(@AuthenticatedMember Member member) {
        // Todo.
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<Void> getPostList() {
        // Todo.
        return ApiResponse.ok(null);
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

    @Operation(summary = "이미지 presigned URL 발급")
    @PostMapping("/image")
    public ApiResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        return ApiResponse.ok(postCommandService.getPresignedUrl(request));
    }
}
