package org.goorm.veri.veribe.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.comment.service.CommentQueryService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.dto.response.PostDetailResponse;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.PostRepository;
import org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResult;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.exception.http.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final CommentQueryService commentQueryService;

    public Post getPosById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND));
    }

    public PostDetailResponse getPostDetail(Long postId) {
        Member requester = AuthUtil.getCurrentMember();

        PostDetailQueryResult postDetail = postRepository.findPostDetailsById(postId, requester)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND));

        List<PostDetailResponse.CommentResponse> comments = commentQueryService.getCommentsByPostId(postId);

        return PostDetailResponse.from(postDetail, comments);
    }
}
