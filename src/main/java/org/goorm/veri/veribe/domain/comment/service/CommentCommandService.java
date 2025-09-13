package org.goorm.veri.veribe.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.comment.dto.request.CommentPostRequest;
import org.goorm.veri.veribe.domain.comment.entity.Comment;
import org.goorm.veri.veribe.domain.comment.repository.CommentRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.service.PostQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;
    private final PostQueryService postQueryService;

    @Transactional
    public Long postComment(CommentPostRequest request) {
        Member member = AuthUtil.getCurrentMember();
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
    public Long postReply(Long parentCommentId, String content) {
        Member member = AuthUtil.getCurrentMember();
        Comment parentComment = commentQueryService.getCommentById(parentCommentId);

        Comment reply = Comment.builder()
                .post(parentComment.getPost())
                .author(member)
                .content(content)
                .parent(parentComment)
                .build();

        parentComment.addReply(reply);
        return commentRepository.save(reply).getId();
    }

    public void editComment(Long commentId, String content) {
        Member member = AuthUtil.getCurrentMember();
        Comment comment = commentQueryService.getCommentById(commentId);
        comment.authorizeMember(member.getId());
        comment.editContent(content);

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Member member = AuthUtil.getCurrentMember();
        Comment comment = commentQueryService.getCommentById(commentId);
        comment.authorizeMember(member.getId());

        comment.delete();
        commentRepository.save(comment);
    }
}
