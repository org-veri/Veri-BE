package org.veri.be.post.service;

import me.miensoap.fluent.FluentRepository;
import org.springframework.stereotype.Repository;
import org.veri.be.post.entity.LikePost;
import org.veri.be.post.entity.Post;
import org.veri.be.post.repository.dto.LikeInfoQueryResult;

@Repository
public interface LikePostRepository extends FluentRepository<LikePost, Long> {

    Long countByPostId(Long postId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    default LikeInfoQueryResult getLikeInfoOfPost(Long postId, Long memberId) {
        long likeCount = countByPostId(postId);
        boolean likedByMember = existsByPostIdAndMemberId(postId, memberId);
        return new LikeInfoQueryResult(likeCount, likedByMember);
    }

    Long post(Post post);
}
