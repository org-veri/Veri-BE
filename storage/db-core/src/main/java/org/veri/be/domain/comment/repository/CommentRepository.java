package org.veri.be.domain.comment.repository;

import org.veri.be.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIdIsNull(Long postId);

    List<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(Long postId);

    @Query("""
            SELECT DISTINCT c FROM Comment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.replies r
            LEFT JOIN FETCH r.author
            WHERE c.post.id = :postId AND c.parent IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findByPostIdWithRepliesAndAuthor(@Param("postId") Long postId);

    @Query("""
            SELECT DISTINCT c FROM Comment c
            LEFT JOIN FETCH c.post p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH c.author
            WHERE c.id = :commentId
            """)
    Optional<Comment> findByIdWithPostAndAuthor(@Param("commentId") Long commentId);
}
