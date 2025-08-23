package org.goorm.veri.veribe.domain.post.repository;

import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT new org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResponse(" +
            "p.id, p.title, p.content, p.author, " +
            "(SELECT COUNT(l) FROM LikePost l WHERE l.post = p), " +
            "(CASE WHEN EXISTS (SELECT l2 FROM LikePost l2 WHERE l2.post = p AND l2.member = :currentUser) THEN true ELSE false END)) " +
            "FROM Post p " +
            "WHERE p.id = :postId")
    Optional<PostDetailQueryResponse> findPostDetailsById(
            @Param("postId") Long postId,
            @Param("currentUser") Member currentUser
    );
}
