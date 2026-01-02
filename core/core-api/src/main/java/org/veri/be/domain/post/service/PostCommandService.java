package org.veri.be.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.service.BookService;
import org.veri.be.domain.card.entity.CardErrorInfo;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
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
import org.veri.be.lib.exception.ApplicationException;

import static org.veri.be.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PostQueryService postQueryService;
    private final BookService bookService;
    private final StorageService storageService;
    private final LikePostRepository likePostRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createPost(PostCreateRequest request, Long memberId) {
        Book book = this.bookService.getBookById(request.bookId());
        Member member = memberRepository.getReferenceById(memberId);

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
    public void deletePost(Long postId, Long memberId) {
        Post post = this.postQueryService.getPostById(postId);
        post.authorizeOrThrow(memberId);
        this.postRepository.deleteById(postId);
    }

    @Transactional
    public void publishPost(Long postId, Long memberId) {
        Post post = this.postQueryService.getPostById(postId);
        Member member = memberRepository.getReferenceById(memberId);
        post.publishBy(member);
        postRepository.save(post);
    }

    @Transactional
    public void unPublishPost(Long postId, Long memberId) {
        Post post = this.postQueryService.getPostById(postId);
        Member member = memberRepository.getReferenceById(memberId);
        post.unpublishBy(member);
        postRepository.save(post);
    }

    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        if (request.contentLength() > MB) {
            throw ApplicationException.of(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw ApplicationException.of(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrlOfDefault(
                request.contentType(),
                request.contentLength()
        );
    }

    @Transactional
    public LikeInfoResponse likePost(Long postId, Long memberId) {
        if (likePostRepository.existsByPostIdAndMemberId(postId, memberId)) {
            return new LikeInfoResponse(likePostRepository.countByPostId(postId), true);
        }

        LikePost likePost = LikePost.builder()
                .post(postQueryService.getPostById(postId))
                .member(memberRepository.getReferenceById(memberId))
                .build();

        likePostRepository.save(likePost);
        return new LikeInfoResponse(likePostRepository.countByPostId(postId), true);
    }

    @Transactional
    public LikeInfoResponse unlikePost(Long postId, Long memberId) {
        likePostRepository.deleteByPostIdAndMemberId(postId, memberId);

        return new LikeInfoResponse(likePostRepository.countByPostId(postId), false);
    }
}
