package org.veri.be.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.comment.dto.request.CommentPostRequest;
import org.veri.be.comment.entity.Comment;
import org.veri.be.comment.repository.CommentRepository;
import org.veri.be.member.entity.Member;
import org.veri.be.post.entity.Post;
import org.veri.be.post.service.PostQueryService;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;
    private final PostQueryService postQueryService;
    private final Clock clock;

    @Transactional
    public Long postComment(CommentPostRequest request, Member member) {
        Post post = postQueryService.getPostById(request.postId());

        Comment comment = Comment.builder()
                .post(post)
                .author(member)
                .content(request.content())
                .build();

        post.addComment(comment);
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public Long postReply(Long parentCommentId, String content, Member member) {
        Comment parentComment = commentQueryService.getCommentById(parentCommentId);

        Comment reply = parentComment.replyBy(member, content);
        return commentRepository.save(reply).getId();
    }

    public void editComment(Long commentId, String content, Member member) {
        Comment comment = commentQueryService.getCommentById(commentId);
        comment.editBy(member, content);

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Member member) {
        Comment comment = commentQueryService.getCommentById(commentId);
        comment.deleteBy(member, clock);
        commentRepository.save(comment);
    }
}
