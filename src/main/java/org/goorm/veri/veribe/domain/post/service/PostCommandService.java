package org.goorm.veri.veribe.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.card.exception.CardErrorInfo;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.post.dto.request.PostCreateRequest;
import org.goorm.veri.veribe.domain.post.dto.response.LikeInfoResponse;
import org.goorm.veri.veribe.domain.post.entity.LikePost;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.repository.LikePostRepository;
import org.goorm.veri.veribe.domain.post.repository.PostRepository;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.goorm.veri.veribe.global.storage.service.StorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PostQueryService postQueryService;
    private final StorageService storageService;
    private final LikePostRepository likePostRepository;

    @Transactional
    public Long createPost(PostCreateRequest request, Member member) {
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(member)
                .build();

        for (int i = 0; i < request.images().size(); i++) {
            post.addImage(request.images().get(i), i + 1);
        }

        this.postRepository.save(post);
        return post.getId();
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = this.postQueryService.getPostById(postId);
        post.authorizeMember(AuthUtil.getCurrentMember().getId());
        this.postRepository.deleteById(postId);
    }


    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        if (request.contentLength() > MB) {
            throw new BadRequestException(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw new BadRequestException(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrlOfDefault(
                request.contentType(),
                request.contentLength()
        );
    }

    @Transactional
    public LikeInfoResponse likePost(Long postId, Member member) {
        if (likePostRepository.existsByPostIdAndMemberId(postId, member.getId())) {
            return new LikeInfoResponse(likePostRepository.countByPostId(postId), true);
        }

        LikePost likePost = LikePost.builder()
                .post(postQueryService.getPostById(postId))
                .member(member)
                .build();

        likePostRepository.save(likePost);
        return new LikeInfoResponse(likePostRepository.countByPostId(postId), true);
    }

    @Transactional
    public LikeInfoResponse unlikePost(Long postId, Member member) {
        likePostRepository.deleteByPostIdAndMemberId(postId, member.getId());

        return new LikeInfoResponse(likePostRepository.countByPostId(postId), false);
    }
}
