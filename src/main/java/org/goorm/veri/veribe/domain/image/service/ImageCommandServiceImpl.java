package org.goorm.veri.veribe.domain.image.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class ImageCommandServiceImpl implements ImageCommandService {
    @Override
    public String extractTextFromBook(MultipartFile file) throws Exception {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/Users/Jinyoung/ocr/tessdata"); // tessdata 경로 설정 (macOS)
        tesseract.setLanguage("kor+eng"); // 한글+영어
        tesseract.setPageSegMode(6); // 6: 단락 분석

        File convFile = new File("/Users/Jinyoung/ocr/temp/" + file.getOriginalFilename());
        file.transferTo(convFile);

        try {
            return tesseract.doOCR(convFile);
        } catch (TesseractException e) {
            throw new Exception("OCR Processing Failed", e);
        } finally {
            if (convFile.exists()) {
                convFile.delete();
            }
        }
    }
}