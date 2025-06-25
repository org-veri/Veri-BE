package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.goorm.veri.veribe.global.storage.service.StorageUtil;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class ImageCommandServiceImpl implements ImageCommandService {

    private final StorageService storageService;
    private final ImageRepository imageRepository;


    public void insertImageUrl(String imageUrl, Member member){
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();

        imageRepository.save(image);
    }


    @Override
    public String extractTextFromBook(MultipartFile file) throws Exception {

        BufferedImage preprocessed = preprocessImage(convertFileToImage(file));

        String email = "test1";

        PresignedUrlResponse presigned = getPresignedUrl(file.getContentType());

        storageService.uploadFileToS3(preprocessed, presigned.imageKey());

        URL s3Url = new URL(presigned.publicUrl());

        Tesseract tesseract = createConfiguredTesseract();

        try (InputStream inputStream = s3Url.openStream()) {
            return tesseract.doOCR(ImageIO.read(inputStream));
        } catch (TesseractException e) {
            throw new Exception("OCR Processing Failed", e);
        }
    }

    public BufferedImage convertFileToImage(MultipartFile file) throws IOException {
        return ImageIO.read(file.getInputStream());
    }

    public BufferedImage preprocessImage(BufferedImage input) {
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

    public PresignedUrlResponse getPresignedUrl(String request) {
        int expirationMinutes = 5;
        long allowedSize = MB; // 1MB
        String prefix = "public";

        if (!StorageUtil.isImage(request)) throw new CardException(CardErrorCode.UNSUPPORTED_IMAGE_TYPE);

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