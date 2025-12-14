package org.veri.be.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.comment.service.CommentQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.post.controller.enums.PostSortType;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.domain.post.dto.response.PostFeedResponseItem;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.LikePostRepository;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.lib.exception.http.NotFoundException;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final LikePostQueryService likePostQueryService;
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
        Member requester = MemberContext.getMemberOrThrow();

        Post post = getPostById(postId);
        DetailLikeInfoQueryResult likeInfo = likePostQueryService.getDetailLikeInfoOfPost(postId, requester.getId());
        List<PostDetailResponse.CommentResponse> comments = commentQueryService.getCommentsByPostId(postId);

        return PostDetailResponse.from(post, likeInfo, comments);
    }
}
