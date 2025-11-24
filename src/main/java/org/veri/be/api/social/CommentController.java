package org.veri.be.api.social;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.veri.be.domain.comment.dto.request.CommentEditRequest;
import org.veri.be.domain.comment.dto.request.CommentPostRequest;
import org.veri.be.domain.comment.dto.request.ReplyPostRequest;
import org.veri.be.domain.comment.service.CommentCommandService;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "소셜")
@Tag(name = "댓글")
@RequestMapping("/api/v1/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentCommandService commentCommandService;

    @PostMapping()
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    public ApiResponse<Long> postComment(@RequestBody CommentPostRequest request) {
        return ApiResponse.created(commentCommandService.postComment(request));
    }

    @PostMapping("/reply")
    @Operation(summary = "대댓글 작성", description = "댓글에 대한 대댓글을 작성합니다.")
    public ApiResponse<Long> postReply(@RequestBody ReplyPostRequest request) {
        return ApiResponse.created(commentCommandService.postReply(request.parentCommentId(), request.content()));
    }

    @PatchMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글의 내용을 수정합니다.")
    public void editComment(
            @PathVariable Long commentId,
            @RequestBody CommentEditRequest request
    ) {
        commentCommandService.editComment(commentId, request.content());
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public void deleteComment(@PathVariable Long commentId) {
        commentCommandService.deleteComment(commentId);
    }
}
