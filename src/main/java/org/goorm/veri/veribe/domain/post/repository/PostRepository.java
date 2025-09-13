package org.goorm.veri.veribe.domain.post.repository;

import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT new org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult(
                p.id, p.title, p.content,
                pi.imageUrl,
                p.author,
                p.book,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p AND c.parent = null),
                p.createdAt
            )
            FROM Post p
            LEFT JOIN PostImage pi ON pi.post = p AND pi.displayOrder = 1
            """
    )
    Page<PostFeedQueryResult> getPostFeeds(Pageable pageable);


    @Query("""
            SELECT new org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult(
                p.id, p.title, p.content,
                pi.imageUrl,
                p.author,
                p.book,
                (SELECT COUNT(l) FROM LikePost l WHERE l.post = p),
                (SELECT COUNT(c) FROM Comment c WHERE c.post = p AND c.parent = null),
                p.createdAt
            )
            FROM Post p
            LEFT JOIN PostImage pi ON pi.post = p AND pi.displayOrder = 1
            WHERE p.author.id = :memberId
            """
    )
    List<PostFeedQueryResult> findAllByAuthorId(Long memberId);
}
