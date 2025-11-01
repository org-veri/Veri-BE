package org.veri.be.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;
import org.veri.be.domain.post.repository.dto.LikeInfoQueryResult;

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

    @Query("""
            SELECT new org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult(
                COLLECT(lp.member) AS likedMembers,
                COUNT(lp) AS likeCount,
                CASE
                    WHEN (COUNT(CASE WHEN lp.member.id = :memberId THEN 1 ELSE NULL END) > 0)
                    THEN true
                    ELSE false
                END
                )
            FROM LikePost lp
            WHERE lp.post.id = :postId
            GROUP BY lp.post.id
            """
    )
    DetailLikeInfoQueryResult getDetailLikeInfoOfPost(Long postId, Long memberId);

    Long post(Post post);
}
