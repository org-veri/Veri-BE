package org.goorm.veri.veribe.domain.image.service;

import org.springframework.web.multipart.MultipartFile;


public interface ImageCommandService {
    String processImageOcrAndSave(String imageUrl) throws Exception;
}