package org.veri.be.comment.service;

import org.veri.be.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndParentIdIsNull(Long postId);

    List<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(Long postId);

    @Query("""
            SELECT DISTINCT c FROM Comment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.replies r
            LEFT JOIN FETCH r.author
            WHERE c.postId = :postId AND c.parent IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findByPostIdWithRepliesAndAuthor(@Param("postId") Long postId);

    void deleteByPostId(Long postId);
}
