package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.exception.DirectoryErrorCode;
import org.goorm.veri.veribe.domain.image.exception.DirectoryException;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.service.enums.FileExtension;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.springframework.stereotype.Service;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class ImageQueryServiceImpl implements ImageQueryService{

    @Override
    public List<String> fetchUploadedImages(String email) throws ImageException, DirectoryException {


        File userDir = new File(fetchUserDirectory(email));

        File[] imageFiles = userDir.listFiles((dir, name) ->
                FileExtension.checkAvailable(name)
        );

        if (imageFiles == null || imageFiles.length == 0) {
            return List.of(); // 업로드된 이미지 없음
        }

        return Arrays.stream(imageFiles)
                .map(this::encodeFileToBase64)
                .toList();
    }

    @Override
    public String fetchUserDirectory(String email) throws DirectoryException{
        String basePath = FileInfo.BASE_UPLOAD_PATH.getValue();
        File userDir = new File(basePath + email);

        if (userDir.exists() && userDir.isDirectory()) {
            return userDir.getAbsolutePath();
        } else {
            throw new DirectoryException(DirectoryErrorCode.NOT_FOUND);
        }
    }


    private String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.ENCODING_FAILED);
        }
    }
}
