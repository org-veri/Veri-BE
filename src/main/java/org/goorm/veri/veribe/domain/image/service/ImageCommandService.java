package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.springframework.web.multipart.MultipartFile;

public interface ImageCommandService {
    String extractTextFromBook(MultipartFile file) throws Exception;
    String makeUserDirectory(String email);
}
