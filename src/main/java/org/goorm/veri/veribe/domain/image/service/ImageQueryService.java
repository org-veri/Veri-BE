package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.dto.response.PageResponse;
import org.goorm.veri.veribe.domain.image.exception.DirectoryException;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.springframework.data.domain.Pageable;


public interface ImageQueryService {
    PageResponse<String> fetchUploadedImages(Long userId, Pageable pageable) throws ImageException, DirectoryException;
}
