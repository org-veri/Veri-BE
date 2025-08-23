package org.goorm.veri.veribe.domain.post.dto.response;

import org.goorm.veri.veribe.domain.common.dto.MemberProfile;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResponse;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        MemberProfile author,
        long likeCount,
        boolean isLiked
) {
    public PostDetailResponse(Long postId, String title, String content, Member author, long likeCount, boolean isLiked) {
        this(postId, title, content, MemberProfile.from(author), likeCount, isLiked);
    }

    public static PostDetailResponse from(PostDetailQueryResponse result) {
        return new PostDetailResponse(
                result.postId(),
                result.title(),
                result.content(),
                result.author(),
                result.likeCount(),
                result.isLiked()
        );
    }
}
