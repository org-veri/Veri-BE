package org.veri.be.domain.post.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.member.repository.dto.MemberProfileQueryResult;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.repository.LikePostRepository;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikePostQueryService {

    private final LikePostRepository likeRepository;

    public DetailLikeInfoQueryResult getDetailLikeInfoOfPost(Long postId, Long memberId) {
        List<MemberProfileQueryResult> memberProfiles = likeRepository.query()
                .fetchJoin(LikePost::getMember)
                .where(likePost -> likePost.getPost().getId()).equalTo(postId)
                .distinct()
                .orderBy(like -> like.getMember().getNickname()).ascending()
                .fetch().stream()
                .map(like -> new MemberProfileQueryResult(
                        like.getMember().getId(),
                        like.getMember().getNickname(),
                        like.getMember().getProfileImageUrl()))
                .toList();

        long likeCount = memberProfiles.size();
        boolean isLiked = memberProfiles.stream().allMatch(member -> member.id().equals(memberId));

        return new DetailLikeInfoQueryResult(memberProfiles, likeCount, isLiked);
    }
}
