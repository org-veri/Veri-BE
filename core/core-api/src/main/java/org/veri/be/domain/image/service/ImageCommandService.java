package org.veri.be.domain.image.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.image.entity.Image;
import org.veri.be.domain.image.exception.ImageErrorCode;
import org.veri.be.domain.image.repository.ImageRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.lib.exception.ApplicationException;

@Service
@RequiredArgsConstructor
public class ImageCommandService {

    private final ImageRepository imageRepository;
    private final OcrService mistralOcrService;
    private final MemberRepository memberRepository;

    @Transactional
    public String processWithMistral(Long memberId, String imageUrl) {
        insertImageUrl(imageUrl, memberId);
        try {
            return mistralOcrService.extract(imageUrl);
        } catch (Exception _) {
            throw ApplicationException.of(ImageErrorCode.OCR_PROCESSING_FAILED);
        }
    }

    private void insertImageUrl(String imageUrl, Long memberId) {
        Member member = memberRepository.getReferenceById(memberId);
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();
        imageRepository.save(image);
    }
}
