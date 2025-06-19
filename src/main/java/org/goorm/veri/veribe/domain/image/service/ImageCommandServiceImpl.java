package org.goorm.veri.veribe.domain.image.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class ImageCommandServiceImpl implements ImageCommandService {
    @Override
    public String extractTextFromBook(MultipartFile file) throws Exception {

        String email = "test1";

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(FileInfo.BASE_META_PATH.getValue()); // tessdata 언어 경로 설정, 로컬 기준이라 production에선 수정.
        tesseract.setLanguage(FileInfo.SUPPORT_LANGUAGE.getValue()); // 한글+영어
        tesseract.setPageSegMode(6);

        String targetDirectory = makeUserDirectory(email);

        // tessdata 경로 설정, 로컬 기준이라 production에선 수정.
        File convFile = new File(targetDirectory + file.getOriginalFilename());
        file.transferTo(convFile);

        try {
            return tesseract.doOCR(convFile);
        } catch (TesseractException e) {
            throw new Exception("OCR Processing Failed", e);
        }
    }

    @Override
    public String makeUserDirectory(String email) {
        String basePath = FileInfo.BASE_UPLOAD_PATH.getValue();
        File userDir = new File(basePath + email);

        if (!userDir.exists()) {
            boolean created = userDir.mkdirs(); // 디렉토리 생성
            if (created) {
                System.out.println("디렉토리 생성 완료: " + userDir.getAbsolutePath());
            } else {
                System.err.println("디렉토리 생성 실패");
            }
        } else {
            System.out.println("이미 디렉토리 존재함: " + userDir.getAbsolutePath());
        }

        return userDir.getAbsolutePath() + File.separator;
    }
}