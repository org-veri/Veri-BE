package org.goorm.veri.veribe.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.comment.entity.Comment;
import org.goorm.veri.veribe.domain.comment.repository.CommentRepository;
import org.goorm.veri.veribe.domain.post.dto.response.PostDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public List<PostDetailResponse.CommentResponse> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndParentIdIsNull(postId);

        return comments.stream()
                .map(PostDetailResponse.CommentResponse::fromEntity)
                .toList();
    }
}
