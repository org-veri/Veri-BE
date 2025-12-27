package org.veri.be.domain.image.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.image.entity.Image;
import org.veri.be.domain.image.exception.ImageErrorCode;
import org.veri.be.domain.image.repository.ImageRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.lib.exception.ApplicationException;

@Service
@RequiredArgsConstructor
public class ImageCommandService {

    private final ImageRepository imageRepository;
    private final OcrService mistralOcrService;

    @Transactional
    public String processWithMistral(Member member, String imageUrl) {
        insertImageUrl(imageUrl, member);
        try {
            return mistralOcrService.extract(imageUrl);
        } catch (Exception _) {
            throw ApplicationException.of(ImageErrorCode.OCR_PROCESSING_FAILED);
        }
    }

    private void insertImageUrl(String imageUrl, Member member) {
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();
        imageRepository.save(image);
    }
}
