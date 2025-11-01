package org.goorm.veri.veribe.domain.post.repository;

import org.goorm.veri.veribe.domain.post.entity.LikePost;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.dto.DetailLikeInfoQueryResult;
import org.goorm.veri.veribe.domain.post.repository.dto.LikeInfoQueryResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LikePostRepository extends JpaRepository<LikePost, Long> {

    Long countByPostId(Long postId);

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    default LikeInfoQueryResult getLikeInfoOfPost(Long postId, Long memberId) {
        long likeCount = countByPostId(postId);
        boolean likedByMember = existsByPostIdAndMemberId(postId, memberId);
        return new LikeInfoQueryResult(likeCount, likedByMember);
    }

    @Query(
            """
                    SELECT new org.goorm.veri.veribe.domain.post.repository.dto.DetailLikeInfoQueryResult(
                           COLLECT(lp.member) AS likedMembers,
                           COUNT(lp) AS likeCount,
                           SUM(CASE WHEN lp.member.id = :memberId THEN 1 ELSE 0 END) AS isLiked
                    )
                    FROM LikePost lp
                    WHERE lp.post.id = :postId
                    GROUP BY lp.post.id
                    """
    )
    DetailLikeInfoQueryResult getDetailLikeInfoOfPost(Long postId, Long memberId);

    Long post(Post post);
}
