package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.dto.response.PageResponse;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageQueryServiceImpl implements ImageQueryService{
    private final ImageRepository imageRepository;

    @Override
    public PageResponse<List<String>> fetchUploadedImages(Long userId, Pageable pageable) {
        Page<String> imageUrls = imageRepository.findByMemberId(userId, pageable);

        if(imageUrls.isEmpty()){
            return PageResponse.empty(pageable);
        }

        return PageResponse.of(imageUrls.getContent(), imageUrls.getNumber(), imageUrls.getSize(),
                imageUrls.getTotalElements(), imageUrls.getTotalPages());
    }
}
