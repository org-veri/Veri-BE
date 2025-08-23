package org.goorm.veri.veribe.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.goorm.veri.veribe.domain.comment.entity.Comment;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.entity.BaseEntity;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.exception.http.ForbiddenException;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    private Boolean isPublic = true;

    public int getCommentCount() {
        return comments.size();
    }

    public void authorizeMember(Long id) {
        if (!this.author.getId().equals(id)) {
            throw new ForbiddenException(CommonErrorInfo.DOES_NOT_HAVE_PERMISSION);
        }
    }
}
