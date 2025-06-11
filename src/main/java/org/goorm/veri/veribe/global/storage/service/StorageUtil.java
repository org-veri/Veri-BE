package org.goorm.veri.veribe.global.storage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StorageUtil {

    @Value("${cloud.storage.prefix}")
    private String prefix;

    public String generateUniqueKey(String contentType) {
        String extension = contentType.substring(contentType.lastIndexOf('/') + 1);

        return String.format("%s/%s.%s", prefix, UUID.randomUUID(), extension);
    }
}
