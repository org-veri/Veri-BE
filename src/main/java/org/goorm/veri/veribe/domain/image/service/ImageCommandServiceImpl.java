package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.image.service.enums.FileExtension;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.goorm.veri.veribe.domain.image.service.enums.FileSize;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.goorm.veri.veribe.global.storage.service.StorageUtil;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class ImageCommandServiceImpl implements ImageCommandService {

    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    private void insertImageUrl(String imageUrl, Member member){
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();

        imageRepository.save(image);
    }

    @Override
    public String processImageOcrAndSave(MultipartFile file) throws Exception {
        // TODO: 컨트롤러에서 Member 전달 받는 로직 추가 후 아랫 줄 삭제.
        Member member = memberRepository.findById(1L).orElseThrow();


        if (FileSize.PERMITTED_SIZE.getSize() < file.getSize()) {
            throw new ImageException(ImageErrorCode.SIZE_EXCEEDED);
        }

        if (FileExtension.checkAvailable(file.getName())){
            throw new ImageException(ImageErrorCode.UNSUPPORTED_TYPE);
        }

        BufferedImage original = ImageIO.read(file.getInputStream()); // 원본 이미지, 사용자가 업로드 한 이미지를 불러올 떄 사용
        BufferedImage preprocessed = preprocessImage(convertFileToImage(file)); // 전처리 이미지, OCR 인식률 높이기 위한 목적.

        PresignedUrlResponse presignedOriginal = getPresignedUrl(file.getContentType());
        PresignedUrlResponse presignedPreprocessed = getPresignedUrl(file.getContentType());

        storageService.uploadImageToS3(original, presignedOriginal.imageKey(), FileExtension.convertMimeToExtension(file.getContentType()));
        storageService.uploadImageToS3(preprocessed, presignedPreprocessed.imageKey(), FileExtension.convertMimeToExtension(file.getContentType()));

        String ocrResult = extractTextFromBook(presignedPreprocessed);

        insertImageUrl(presignedOriginal.publicUrl(), member);

        return ocrResult;
    }

    private String extractTextFromBook(PresignedUrlResponse presignedUrlResponse) throws Exception {
        URL s3Url = new URL(presignedUrlResponse.publicUrl());
        Tesseract tesseract = createConfiguredTesseract();

        try (InputStream inputStream = s3Url.openStream()) {
            return tesseract.doOCR(ImageIO.read(inputStream));
        } catch (TesseractException e) {
            throw new Exception("OCR Processing Failed", e);
        }
    }

    private BufferedImage convertFileToImage(MultipartFile file) throws IOException {
        return ImageIO.read(file.getInputStream());
    }

    private BufferedImage preprocessImage(BufferedImage input) {
        BufferedImage gray = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(input, 0, 0, null);
        g.dispose();

        WritableRaster raster = gray.getRaster();
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                int value = raster.getSample(x, y, 0);
                int newValue = value < 140 ? 0 : 255;
                raster.setSample(x, y, 0, newValue);
            }
        }

        return gray;
    }

    private PresignedUrlResponse getPresignedUrl(String request) {
        int expirationMinutes = 5;
        long allowedSize = MB; // 1MB
        String prefix = "public";

        if (!StorageUtil.isImage(request)) throw new ImageException(ImageErrorCode.UNSUPPORTED_TYPE);

        return storageService.generatePresignedUrl(
                request,
                Duration.ofMinutes(expirationMinutes),
                allowedSize,
                prefix
        );
    }

    private Tesseract createConfiguredTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(FileInfo.BASE_META_PATH.getValue()); // tessdata 언어 경로 설정, 로컬 기준이라 production에선 수정.
        tesseract.setLanguage(FileInfo.SUPPORT_LANGUAGE.getValue()); // 한글+영어
        tesseract.setPageSegMode(3);

        return tesseract;
    }
}