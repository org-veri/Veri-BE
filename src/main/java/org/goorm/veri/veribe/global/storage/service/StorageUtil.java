package org.goorm.veri.veribe.global.storage.service;

import java.util.UUID;

public final class StorageUtil {

    public static String generateUniqueKey(String contentType, String prefix) {
        String extension = contentType.substring(contentType.lastIndexOf('/') + 1);

        return String.format("%s/%s.%s", prefix, UUID.randomUUID(), extension);
    }

    public static boolean isImage(String contentType) {
        return contentType.startsWith("image/");
    }
}
