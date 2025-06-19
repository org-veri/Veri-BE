package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.exception.DirectoryErrorCode;
import org.goorm.veri.veribe.domain.image.exception.DirectoryException;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.service.enums.FileInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ImageQueryServiceImpl implements ImageQueryService{

    @Override
    public List<String> fetchUploadedImages(String email) throws ImageException, DirectoryException {
        File userDir = new File(fetchUserDirectory(email));

        if (!userDir.exists() || !userDir.isDirectory()) {
            throw new DirectoryException(DirectoryErrorCode.NOT_FOUND);
        }

        File[] imageFiles = userDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png")
        );

        if (imageFiles == null || imageFiles.length == 0) {
            return List.of(); // 업로드된 이미지 없음
        }

        return List.of(imageFiles).stream()
                .map(file -> {
                    try {
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        return Base64.getEncoder().encodeToString(fileContent);
                    } catch (IOException e) {
                        throw new ImageException(ImageErrorCode.ENCODING_FAILED);
                    }
                })
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
}
