package org.goorm.veri.veribe.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.card.exception.CardErrorInfo;
import org.goorm.veri.veribe.domain.post.entity.Post;
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


    @Transactional
    public void deletePost(Long postId) {
        Post post = this.postQueryService.getPosById(postId);
        post.authorizeMember(AuthUtil.getCurrentMember().getId());
        this.postRepository.deleteById(postId);
    }


    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        String prefix = "public";

        if (request.contentLength() > 3 * MB) {
            throw new BadRequestException(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw new BadRequestException(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrlOfDefault(
                request.contentType(),
                request.contentLength()
        );
    }
}
