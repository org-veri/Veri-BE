package org.goorm.veri.veribe.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.comment.service.CommentQueryService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.controller.enums.PostSortType;
import org.goorm.veri.veribe.domain.post.dto.response.PostDetailResponse;
import org.goorm.veri.veribe.domain.post.dto.response.PostFeedResponseItem;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.LikePostRepository;
import org.goorm.veri.veribe.domain.post.repository.PostRepository;
import org.goorm.veri.veribe.domain.post.repository.dto.LikeInfoQueryResult;
import org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.exception.http.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final LikePostRepository likePostRepository;
    private final CommentQueryService commentQueryService;

    public Page<PostFeedQueryResult> getPostFeeds(
            int page, int size, PostSortType sortType
    ) {
        Pageable pageRequest = PageRequest.of(page, size, sortType.getSort());
        return postRepository.getPostFeeds(pageRequest);
    }

    public List<PostFeedResponseItem> getPostsOfMember(Long memberId) {
        return postRepository.findAllByAuthorId(memberId).stream().map(PostFeedResponseItem::from).toList();
    }

    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(CommonErrorInfo.RESOURCE_NOT_FOUND));
    }

    public PostDetailResponse getPostDetail(Long postId) {
        Member requester = AuthUtil.getCurrentMember();

        Post post = getPostById(postId);
        LikeInfoQueryResult likeInfo = likePostRepository.getLikeInfoOfPost(postId, requester.getId());
        List<PostDetailResponse.CommentResponse> comments = commentQueryService.getCommentsByPostId(postId);

        return PostDetailResponse.from(post, likeInfo, comments);
    }
}
