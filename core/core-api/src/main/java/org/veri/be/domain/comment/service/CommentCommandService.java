package org.veri.be.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.comment.dto.request.CommentPostRequest;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.repository.CommentRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.service.PostQueryService;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;
    private final PostQueryService postQueryService;
    private final Clock clock;
    private final MemberRepository memberRepository;

    @Transactional
    public Long postComment(CommentPostRequest request, Long memberId) {
        Post post = postQueryService.getPostById(request.postId());
        Member member = memberRepository.getReferenceById(memberId);

        Comment comment = Comment.builder()
                .post(post)
                .author(member)
                .content(request.content())
                .build();

        post.addComment(comment);
        return commentRepository.save(comment).getId();
    }

    @Transactional
    public Long postReply(Long parentCommentId, String content, Long memberId) {
        Comment parentComment = commentQueryService.getCommentById(parentCommentId);
        Member member = memberRepository.getReferenceById(memberId);

        Comment reply = parentComment.replyBy(member, content);
        return commentRepository.save(reply).getId();
    }

    public void editComment(Long commentId, String content, Long memberId) {
        Comment comment = commentQueryService.getCommentById(commentId);
        Member member = memberRepository.getReferenceById(memberId);
        comment.editBy(member, content);

        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentQueryService.getCommentById(commentId);
        Member member = memberRepository.getReferenceById(memberId);
        comment.deleteBy(member, clock);
        commentRepository.save(comment);
    }
}
