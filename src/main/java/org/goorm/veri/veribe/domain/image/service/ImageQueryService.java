package org.goorm.veri.veribe.domain.image.service;

import org.goorm.veri.veribe.domain.image.exception.DirectoryException;
import org.goorm.veri.veribe.domain.image.exception.ImageException;

import java.io.IOException;
import java.util.List;

public interface ImageQueryService {
    public List<String> fetchUploadedImages(String email) throws ImageException, DirectoryException;
    public String fetchUserDirectory(String email) throws DirectoryException;
}
