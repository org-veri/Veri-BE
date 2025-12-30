package org.veri.be.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.comment.service.PostExistenceProvider;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostExistenceProviderService implements PostExistenceProvider {

    private final PostRepository postRepository;

    @Override
    public void ensureExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
    }
}
