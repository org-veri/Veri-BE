package org.goorm.veri.veribe.domain.image.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageCommandService {
    String extractTextFromBook(MultipartFile file) throws Exception;
    String makeUserDirectory(String email);
}
