package org.goorm.veri.veribe.domain.image.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorInfo;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.exception.http.InternalServerException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageCommandService {

    private final ImageRepository imageRepository;
    private final MistralOcrService mistralOcrService;

    @Transactional
    public String processWithMistral(Member member, String imageUrl) {
        insertImageUrl(imageUrl, member);
        try {
            return mistralOcrService.doExtract(imageUrl);
        } catch (InternalServerException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
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
