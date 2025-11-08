package org.veri.be.domain.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.global.entity.Authorizable;
import org.veri.be.global.entity.BaseEntity;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.lib.exception.http.ForbiddenException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends BaseEntity implements Authorizable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder.Default
    @OrderBy("createdAt ASC")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isRoot() {
        return parent == null;
    }

    public Comment addReply(Comment reply) {
        this.replies.add(reply);
        return reply;
    }

    public Comment editContent(String content) {
        this.content = content;
        return this;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public void authorizeMember(Long id) {
        if (!this.author.getId().equals(id)) {
            throw new ForbiddenException(CommonErrorInfo.DOES_NOT_HAVE_PERMISSION);
        }
    }
}
