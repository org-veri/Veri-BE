package org.veri.be.comment.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.comment.entity.Comment;
import org.veri.be.comment.service.CommentRepository;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.exception.ApplicationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdWithRepliesAndAuthor(postId);
    }

    public Comment getCommentById(Long parentCommentId) {
        return this.commentRepository.findById(parentCommentId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND));
    }
}
