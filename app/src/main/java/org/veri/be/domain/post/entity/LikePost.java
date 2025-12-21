package org.veri.be.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.veri.be.domain.member.entity.Member;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "post_like")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikePost that)) return false;
        return member != null && post != null
                && member.getId() != null && post.getId() != null
                && member.getId().equals(that.getMember().getId())
                && post.getId().equals(that.getPost().getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                member != null ? member.getId() : 0,
                post != null ? post.getId() : 0
        );
    }
}
