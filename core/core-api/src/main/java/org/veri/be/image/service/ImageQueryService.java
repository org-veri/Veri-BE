package org.veri.be.image.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.global.response.PageResponse;
import org.veri.be.image.service.ImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageQueryService {
    private final ImageRepository imageRepository;

    public PageResponse<List<String>> fetchUploadedImages(Long memberId, Pageable pageable) {
        Page<String> imageUrls = imageRepository.findByMemberId(memberId, pageable);

        if (imageUrls.isEmpty()) {
            return PageResponse.empty(pageable);
        }

        return PageResponse.of(imageUrls.getContent(), imageUrls.getNumber(), imageUrls.getSize(),
                imageUrls.getTotalElements(), imageUrls.getTotalPages());
    }
}
