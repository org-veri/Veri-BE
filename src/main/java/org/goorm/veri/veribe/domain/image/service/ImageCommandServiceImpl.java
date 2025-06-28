package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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
import java.awt.image.WritableRaster;
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
    public String processImageOcrAndSave(String imageUrl, Member member) throws Exception {
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

    private Tesseract createConfiguredTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(ocrConfigData.getTessdataPath());
        tesseract.setLanguage(ocrConfigData.getLanguage());
        tesseract.setPageSegMode(3);

        return tesseract;
    }
}
