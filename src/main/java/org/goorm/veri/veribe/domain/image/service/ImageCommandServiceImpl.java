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

            Mat mat = converterToMat.convert(converterToFrame.convert(input));

            Mat resized = new Mat();
            opencv_imgproc.resize(mat, resized, new Size(mat.cols() * 2, mat.rows() * 2));

            Mat gray = new Mat();
            opencv_imgproc.cvtColor(resized, gray, opencv_imgproc.COLOR_BGR2GRAY);

            Mat binary = new Mat();
            opencv_imgproc.adaptiveThreshold(
                    gray,
                    binary,
                    255,
                    opencv_imgproc.ADAPTIVE_THRESH_MEAN_C,
                    opencv_imgproc.THRESH_BINARY,
                    15,
                    10
            );

            return converterToFrame.convert(converterToMat.convert(binary));
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
