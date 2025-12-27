package org.veri.be.domain.post.repository;

import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;
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
            SELECT new org.veri.be.domain.post.repository.dto.PostFeedQueryResult(
                p.id, p.title, p.content,
                pi.imageUrl,
                p.author,
                p.book,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
                p.createdAt,
                p.isPublic
            )
            FROM Post p
            LEFT JOIN p.book b
            LEFT JOIN PostImage pi ON pi.post = p AND pi.displayOrder = 1
            WHERE p.isPublic = true
            """
    )
    Page<PostFeedQueryResult> getPostFeeds(Pageable pageable);


    @Query("""
            SELECT new org.veri.be.domain.post.repository.dto.PostFeedQueryResult(
                p.id, p.title, p.content,
                pi.imageUrl,
                p.author,
                p.book,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p),
                p.createdAt,
                p.isPublic
            )
            FROM Post p
            LEFT JOIN p.book b
            LEFT JOIN PostImage pi ON pi.post = p AND pi.displayOrder = 1
            WHERE p.author.id = :memberId
            """
    )
    List<PostFeedQueryResult> findAllByAuthorId(@Param("memberId") Long memberId);

    @Query("""
            SELECT DISTINCT p FROM Post p
            LEFT JOIN FETCH p.images
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.book
            WHERE p.id = :postId
            """)
    Optional<Post> findByIdWithAllAssociations(@Param("postId") Long postId);
}
