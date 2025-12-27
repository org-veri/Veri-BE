package org.veri.be.global.storage.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidStorageKeyGenerator implements StorageKeyGenerator {

    @Override
    public String generate(String contentType, String prefix) {
        String extension = contentType.substring(contentType.lastIndexOf('/') + 1);
        return String.format("%s/%s.%s", prefix, UUID.randomUUID(), extension);
    }
}
