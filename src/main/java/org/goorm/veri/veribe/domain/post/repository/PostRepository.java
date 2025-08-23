package org.goorm.veri.veribe.domain.post.repository;

import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResult;
import org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult(
                p.id, p.title, p.content, p.author,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p AND c.parent = null),
                p.createdAt
            )
            FROM Post p
            """
    )
    Page<PostFeedQueryResult> getPostFeeds(Pageable pageable);


    @Query("""
            SELECT new org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult(
                p.id, p.title, p.content, p.author,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p AND c.parent = null),
                p.createdAt
            )
            FROM Post p
            WHERE p.author.id = :memberId
            """
    )
    List<PostFeedQueryResult> findAllByAuthorId(Long memberId);

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResult(
                p.id, p.title, p.content, p.author,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                CASE WHEN (COUNT(l2) > 0) THEN true ELSE false END,
                p.createdAt
            )
            FROM Post p
            LEFT JOIN LikePost l2 ON l2.post = p AND l2.member = :currentUser
            WHERE p.id = :postId
            GROUP BY p.id, p.title, p.content, p.author
            """
    )
    Optional<PostDetailQueryResult> findPostDetailsById(
            @Param("postId") Long postId,
            @Param("currentUser") Member currentUser
    );
}
