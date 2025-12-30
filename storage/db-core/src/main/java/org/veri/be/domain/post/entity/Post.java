package org.veri.be.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.member.entity.Member;
import org.veri.be.global.entity.Authorizable;
import org.veri.be.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends BaseEntity implements Authorizable {

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

    @JoinColumn(name = "book_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    @Builder.Default
    @OrderBy("displayOrder ASC")
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @Builder.Default
    @OrderBy("createdAt ASC")
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Setter
    @Builder.Default
    private boolean isPublic = true;

    public Comment addComment(Comment comment) {
        this.comments.add(comment);
        return comment;
    }

    public int getCommentCount() {
        return comments.size();
    }

    public void addImage(String imageUrl, long displayOrder) {
        PostImage image = PostImage.builder()
                .post(this)
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();

        this.images.add(image);
    }


    public void publishBy(Member member) {
        authorizeOrThrow(member.getId());
        this.isPublic = true;
    }

    public void unpublishBy(Member member) {
        authorizeOrThrow(member.getId());
        this.isPublic = false;
    }

    @Override
    public boolean authorizeMember(Long id) {
        return this.author.getId().equals(id);
    }
}
