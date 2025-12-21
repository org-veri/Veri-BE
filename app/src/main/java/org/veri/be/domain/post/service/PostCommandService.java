package org.veri.be.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.service.BookService;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.post.dto.request.PostCreateRequest;
import org.veri.be.domain.post.dto.response.LikeInfoResponse;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.LikePostRepository;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.global.storage.service.StorageUtil;
import org.veri.be.lib.exception.http.BadRequestException;

import static org.veri.be.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PostQueryService postQueryService;
    private final BookService bookService;
    private final StorageService storageService;
    private final LikePostRepository likePostRepository;

    @Transactional
    public Long createPost(PostCreateRequest request, Member member) {
        Book book = this.bookService.getBookById(request.bookId());

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(member)
                .book(book)
                .build();

        for (int i = 0; i < request.images().size(); i++) {
            post.addImage(request.images().get(i), i + 1L);
        }

        this.postRepository.save(post);
        return post.getId();
    }

    @Transactional
    public void deletePost(Long postId, Member member) {
        Post post = this.postQueryService.getPostById(postId);
        post.deleteBy(member);
        this.postRepository.deleteById(postId);
    }

    @Transactional
    public void publishPost(Long postId, Member member) {
        Post post = this.postQueryService.getPostById(postId);
        post.publishBy(member);
        postRepository.save(post);
    }

    @Transactional
    public void unPublishPost(Long postId, Member member) {
        Post post = this.postQueryService.getPostById(postId);
        post.unpublishBy(member);
        postRepository.save(post);
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
