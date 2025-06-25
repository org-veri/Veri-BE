package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.goorm.veri.veribe.domain.image.service.enums.FileSize;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.goorm.veri.veribe.global.storage.service.StorageUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;

public interface ImageCommandService {
    String extractTextFromBook(MultipartFile file) throws Exception;
}

//package org.goorm.veri.veribe.domain.image.service;
//
//import lombok.RequiredArgsConstructor;
//import net.sourceforge.tess4j.Tesseract;
//import net.sourceforge.tess4j.TesseractException;
//import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
//import org.goorm.veri.veribe.domain.card.exception.CardException;
//import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
//import org.goorm.veri.veribe.domain.image.exception.ImageException;
//import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
//import org.goorm.veri.veribe.domain.image.service.enums.FileSize;
//import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
//import org.goorm.veri.veribe.global.storage.service.StorageService;
//import org.goorm.veri.veribe.global.storage.service.StorageUtil;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//        import java.awt.image.BufferedImage;
//import java.awt.image.WritableRaster;
//import java.io.*;
//        import java.net.HttpURLConnection;
//import java.net.URL;
//import java.time.Duration;
//import java.util.Objects;
//
//import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;
//
//@Service
//@RequiredArgsConstructor
//public class ImageCommandServiceImpl implements ImageCommandService {
//
//    private final StorageService storageService;
//
//    @Override
//    public String extractTextFromBook(MultipartFile file) throws Exception {
//
//        // TODO: 토큰 도입 후 추출해서 할당하는 걸로 수정.
//        String email = "test1";
//
//        BufferedImage original = ImageIO.read(file.getInputStream());
//        BufferedImage preprocessed = preprocessImage(original);
//
//        Tesseract tesseract = createConfiguredTesseract();
//
//        File savedFile = convertAndSaveFilteredImage(preprocessed, FileInfo.BASE_UPLOAD_PATH.toString());
//
//        try {
//            return tesseract.doOCR(savedFile);
//        } catch (TesseractException e) {
//            throw new Exception("OCR Processing Failed", e);
//        }
//    }
//
//    @Scheduled(fixedRate = 5000)
//    public void syncS3FromFileSystem() {
//        String imageDirPath = FileInfo.BASE_UPLOAD_PATH.toString();
//        File imageDir = new File(imageDirPath);
//
//        if (!imageDir.exists() || !imageDir.isDirectory()) {
//            System.out.println("유효하지 않은 디렉토리 경로입니다.");
//            return;
//        }
//
//        File[] imageFiles = imageDir.listFiles((dir, name) ->
//                name.toLowerCase().endsWith(".png") ||
//                        name.toLowerCase().endsWith(".jpg") ||
//                        name.toLowerCase().endsWith(".jpeg"));
//
//        if (imageFiles == null || imageFiles.length == 0) {
//            System.out.println("이미지가 없습니다.");
//            return;
//        }
//
//        for (File file : imageFiles) {
//            try {
//                BufferedImage image = ImageIO.read(file);
//                if (image != null) {
//
//                    // 여기에 S3 업로드 등 로직 삽입 가능
//                    System.out.println("이미지 로드 성공: " + file.getName());
//                } else {
//                    System.out.println("이미지 로드 실패: " + file.getName());
//                }
//            } catch (IOException e) {
//                System.err.println("이미지 읽기 오류: " + file.getName());
//            }
//        }
//    }
//
//
//
//
//    @Override
//    public String extractTextFromBook(MultipartFile file) throws Exception {
//
//        // 1. MultipartFile → BufferedImage 변환
//        BufferedImage original = ImageIO.read(file.getInputStream());
//
//        // 2. 전처리
//        BufferedImage preprocessed = preprocessImage(original);
//
//        String email = "test1";
//
//        PresignedUrlResponse presigned = getPresignedUrl(file.getContentType());
//
//
//
//        storageService.uploadFileToS3(preprocessed, presigned.imageKey());
//
//        URL s3Url = new URL(presigned.publicUrl());
//
//        Tesseract tesseract = createConfiguredTesseract();
//
//        try (InputStream inputStream = s3Url.openStream()) {
//            return tesseract.doOCR(ImageIO.read(inputStream));
//        } catch (TesseractException e) {
//            throw new Exception("OCR Processing Failed", e);
//        }
//    }
//
//    public BufferedImage preprocessImage(BufferedImage input) {
//        BufferedImage gray = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
//        Graphics2D g = gray.createGraphics();
//        g.drawImage(input, 0, 0, null);
//        g.dispose();
//
//        // 2. Thresholding (Binarization)
//        WritableRaster raster = gray.getRaster();
//        for (int y = 0; y < raster.getHeight(); y++) {
//            for (int x = 0; x < raster.getWidth(); x++) {
//                int value = raster.getSample(x, y, 0);
//                int newValue = value < 140 ? 0 : 255; // 기본 threshold 140
//                raster.setSample(x, y, 0, newValue);
//            }
//        }
//
//        return gray;
//    }
//
//    public PresignedUrlResponse getPresignedUrl(String request) {
//        int expirationMinutes = 5;
//        long allowedSize = MB; // 1MB
//        String prefix = "public";
//
//        if (!StorageUtil.isImage(request)) throw new CardException(CardErrorCode.UNSUPPORTED_IMAGE_TYPE);
//
//        return storageService.generatePresignedUrl(
//                request,
//                Duration.ofMinutes(expirationMinutes),
//                allowedSize,
//                prefix
//        );
//    }
//
//
//
//    private Tesseract createConfiguredTesseract() {
//        Tesseract tesseract = new Tesseract();
//        tesseract.setDatapath(FileInfo.BASE_META_PATH.getValue()); // tessdata 언어 경로 설정, 로컬 기준이라 production에선 수정.
//        tesseract.setLanguage(FileInfo.SUPPORT_LANGUAGE.getValue()); // 한글+영어
//        tesseract.setPageSegMode(3);
//
//        return tesseract;
//    }
//
//    private File convertAndSaveFilteredImage(BufferedImage original, String targetDirectory) throws ImageException {
//        // 이미지 크기 검사 (픽셀 크기 말고 용량 기준이면 별도 처리 필요)
//        if (getImageSizeInBytes(original) > FileSize.PERMITTED_SIZE.getSize()) {
//            throw new ImageException(ImageErrorCode.SIZE_EXCEEDED);
//        }
//
//        // 저장할 파일 이름 지정 (예: 현재 시간 기반 이름)
//        String fileName = "filtered_" + System.currentTimeMillis() + ".png";
//        File outputFile = new File(targetDirectory, fileName);
//
//        try {
//            ImageIO.write(original, "png", outputFile);
//        } catch (IOException e) {
//            throw new ImageException(ImageErrorCode.CONVERT_FAILED);
//        }
//
//        return outputFile;
//    }
//
//    private long getImageSizeInBytes(BufferedImage image) {
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            ImageIO.write(image, "png", baos);
//            baos.flush();
//            return baos.size();
//        } catch (IOException e) {
//            return Long.MAX_VALUE; // 오류 시 무조건 초과로 판단
//        }
//    }
//}