package org.veri.be.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.veri.be.member.entity.Member;
import org.veri.be.global.entity.Authorizable;
import org.veri.be.global.entity.BaseEntity;
import org.veri.be.lib.exception.ApplicationException;

import java.time.Clock;
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

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Builder.Default
    @OrderBy("createdAt ASC")
    @BatchSize(size = 100)
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

    public Comment replyBy(Member member, String content) {
        Comment reply = Comment.builder()
                .postId(this.postId)
                .author(member)
                .content(content)
                .parent(this)
                .build();

        return this.addReply(reply);
    }

    public Comment editBy(Member member, String content) {
        authorizeOrThrow(member.getId());
        this.content = content;
        return this;
    }

    public void deleteBy(Member member, Clock clock) {
        authorizeOrThrow(member.getId());
        this.deletedAt = LocalDateTime.now(clock);
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @Override
    public boolean authorizeMember(Long id) {
        return this.author.getId().equals(id);
    }
}
