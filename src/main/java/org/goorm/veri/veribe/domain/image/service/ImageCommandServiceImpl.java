package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.image.service.enums.FileExtension;
import org.goorm.veri.veribe.domain.image.service.enums.FileSize;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.goorm.veri.veribe.global.data.OcrConfigData;
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

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final OcrConfigData ocrConfigData;

    private void insertImageUrl(String imageUrl, Member member){
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();

        imageRepository.save(image);
    }

    @Override
    public String processImageOcrAndSave(String imageUrl) throws Exception {
        // TODO: 추후 인증 컨텍스트에서 사용자 정보 주입
        Member member = memberRepository.findById(1L).orElseThrow();

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