package org.goorm.veri.veribe.domain.image.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.goorm.veri.veribe.domain.image.service.enums.FileSize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
public class ImageCommandServiceImpl implements ImageCommandService {
    @Override
    public String extractTextFromBook(MultipartFile file) throws Exception {

        // TODO: 토큰 도입 후 추출해서 할당하는 걸로 수정.
        String email = "test1";

        Tesseract tesseract = createConfiguredTesseract();

        String targetDirectory = makeUserDirectory(email);

        File savedFile = convertAndSaveMultipartFile(file, targetDirectory);

        try {
            return tesseract.doOCR(savedFile);
        } catch (TesseractException e) {
            throw new Exception("OCR Processing Failed", e);
        }
    }

    // 파일 시스템에서 사용자별 업로드 사진을 구분하기 위해 사용자별 디렉토리 생성.
    @Override
    public String makeUserDirectory(String email) {
        String basePath = FileInfo.BASE_UPLOAD_PATH.getValue();
        File userDir = new File(basePath + email);

        if (!userDir.exists()) {
            userDir.mkdirs(); // 디렉토리 생성
        }

        return userDir.getAbsolutePath() + File.separator;
    }

    private Tesseract createConfiguredTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(FileInfo.BASE_META_PATH.getValue()); // tessdata 언어 경로 설정, 로컬 기준이라 production에선 수정.
        tesseract.setLanguage(FileInfo.SUPPORT_LANGUAGE.getValue()); // 한글+영어
        tesseract.setPageSegMode(3);

        return tesseract;
    }

    private File convertAndSaveMultipartFile(MultipartFile file, String targetDirectory) throws ImageException {
        if(file.getSize() > FileSize.PERMITTED_SIZE.getSize()){
            throw new ImageException(ImageErrorCode.SIZE_EXCEEDED);
        }

        File convFile = new File(targetDirectory, Objects.requireNonNull(file.getOriginalFilename()));
        try {
            file.transferTo(convFile);
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.CONVERT_FAILED);
        }
        return convFile;
    }
}