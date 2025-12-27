package org.veri.be.domain.post.dto.response;

import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;
import org.springframework.data.domain.Page;

import java.util.List;

public record PostFeedResponse(
        List<PostFeedResponseItem> posts,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public PostFeedResponse(Page<PostFeedQueryResult> pageData) {
        this(
                pageData.getContent().stream().map(PostFeedResponseItem::from).toList(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
