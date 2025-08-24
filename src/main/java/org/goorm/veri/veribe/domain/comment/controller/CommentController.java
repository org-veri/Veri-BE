package org.goorm.veri.veribe.domain.comment.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.comment.dto.request.CommentEditRequest;
import org.goorm.veri.veribe.domain.comment.dto.request.CommentPostRequest;
import org.goorm.veri.veribe.domain.comment.dto.request.ReplyPostRequest;
import org.goorm.veri.veribe.domain.comment.service.CommentCommandService;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글 API")
@RequestMapping("/api/v1/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentCommandService commentCommandService;

    @PostMapping()
    public ApiResponse<Long> postComment(@RequestBody CommentPostRequest request) {
        return ApiResponse.created(commentCommandService.postComment(request));
    }

    @PostMapping("/reply")
    public ApiResponse<Long> postReply(@RequestBody ReplyPostRequest request) {
        return ApiResponse.created(commentCommandService.postReply(request.parentCommentId(), request.content()));
    }

    @PatchMapping("/{commentId}")
    public void editComment(
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest request
    ) {
        commentCommandService.editComment(commentId, request.content());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentCommandService.deleteComment(commentId);
    }
}
