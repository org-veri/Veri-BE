package org.veri.be.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.repository.CommentRepository;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.lib.exception.http.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public List<PostDetailResponse.CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(PostDetailResponse.CommentResponse::fromEntity)
                .toList();
    }

    public Comment getCommentById(Long parentCommentId) {
        return this.commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND));
    }
}
