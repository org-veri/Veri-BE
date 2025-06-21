package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.exception.DirectoryException;
import org.goorm.veri.veribe.domain.image.exception.ImageException;

import java.util.List;

public interface ImageQueryService {
    List<String> fetchUploadedImages(String email) throws ImageException, DirectoryException;
    String fetchUserDirectory(String email) throws DirectoryException;
}
