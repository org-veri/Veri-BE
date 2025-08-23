package org.goorm.veri.veribe.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.dto.response.PostDetailResponse;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.PostRepository;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.exception.http.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public Post getPosById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND));
    }

    public PostDetailResponse getPostDetail(Long postId) {
        Member requester = AuthUtil.getCurrentMember();
        return PostDetailResponse.from(postRepository.findPostDetailsById(postId, requester)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND)));
    }
}
