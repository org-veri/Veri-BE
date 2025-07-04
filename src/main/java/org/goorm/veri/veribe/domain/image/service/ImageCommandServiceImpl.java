package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.data.OcrConfigData;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class ImageCommandServiceImpl implements ImageCommandService {

    private final ImageRepository imageRepository;
    private final OcrConfigData ocrConfigData;

    private void insertImageUrl(String imageUrl, Member member) {
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();

        imageRepository.save(image);
    }

    @Override
    public String processImageOcrAndSave(Member member, String imageUrl) throws Exception {
        BufferedImage original;
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            original = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.BAD_REQUEST);
        }
        insertImageUrl(imageUrl, member);
        BufferedImage preprocessed = preprocessImage(original);

        return extractTextFromBufferedImage(preprocessed);
    }

    private String extractTextFromBufferedImage(BufferedImage preprocessed) throws Exception {
        Tesseract tesseract = createConfiguredTesseract();
        try {
            return tesseract.doOCR(preprocessed);
        } catch (TesseractException e) {
            throw new Exception("OCR Processing Failed", e);
        }
    }


    private BufferedImage preprocessImage(BufferedImage input) {
        try (OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
             Java2DFrameConverter converterToFrame = new Java2DFrameConverter()) {

            // 1. Java2D로 Grayscale 변환
            BufferedImage gray = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = gray.createGraphics();
            g.drawImage(input, 0, 0, null);
            g.dispose();

            // 2. BufferedImage → Mat (OpenCV로 변환)
            Mat matGray = converterToMat.convert(converterToFrame.convert(gray));

            opencv_imgproc.GaussianBlur(matGray, matGray, new Size(3, 3), 0);


            // 3. OpenCV adaptive threshold (이진화)
            Mat binary = new Mat();
            opencv_imgproc.adaptiveThreshold(
                    matGray,
                    binary,
                    255,
                    opencv_imgproc.ADAPTIVE_THRESH_MEAN_C,
                    opencv_imgproc.THRESH_BINARY,
                    17,   // blockSize (홀수만 가능, 15~25 사이 실험 추천)
                    7    // C 값 (조정값, 작을수록 더 민감)
            );


            return converterToFrame.convert(converterToMat.convert(binary));

        } catch (Exception e) {
            throw new RuntimeException("Image preprocessing failed", e);
        }
    }

    private Tesseract createConfiguredTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(ocrConfigData.getTessdataPath());
        tesseract.setLanguage(ocrConfigData.getLanguage());
        tesseract.setPageSegMode(6);

        return tesseract;
    }
}
