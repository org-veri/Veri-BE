package org.veri.be.post.dto.response;

import java.util.List;

public record PostListResponse(
        List<PostFeedResponseItem> posts,
        int count
) {

    public static PostListResponse from(List<PostFeedResponseItem> posts) {
        return new PostListResponse(posts, posts.size());
    }
}
