package org.veri.be.domain.post.repository;

import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;

public interface LikePostQueryRepository {

    DetailLikeInfoQueryResult getDetailLikeInfoOfPost(Long postId, Long memberId);

}
