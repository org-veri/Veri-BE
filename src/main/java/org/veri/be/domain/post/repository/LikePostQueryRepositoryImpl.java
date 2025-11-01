package org.veri.be.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;

import java.util.List;

import static org.veri.be.domain.member.entity.QMember.member;
import static org.veri.be.domain.post.entity.QLikePost.likePost;

@RequiredArgsConstructor
public class LikePostQueryRepositoryImpl implements LikePostQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public DetailLikeInfoQueryResult getDetailLikeInfoOfPost(Long postId, Long memberId) {
        List<MemberProfileQueryResult> memberProfiles = queryFactory
                .select(Projections.constructor(MemberProfileQueryResult.class,
                        member.id,
                        member.nickname,
                        member.profileImageUrl
                ))
                .from(likePost)
                .join(likePost.member, member)
                .where(likePost.post.id.eq(postId))
                .fetch();

        Long likeCount = queryFactory
                .select(likePost.count())
                .from(likePost)
                .where(likePost.post.id.eq(postId))
                .fetchOne();

        Boolean isLiked = queryFactory
                .selectOne()
                .from(likePost)
                .where(likePost.post.id.eq(postId), likePost.member.id.eq(memberId))
                .fetchFirst() != null;

        return new DetailLikeInfoQueryResult(memberProfiles, likeCount, isLiked);
    }
}
