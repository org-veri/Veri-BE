package org.veri.be.global.storage.service;

public final class StorageUtil {

    private StorageUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isImage(String contentType) {
        return contentType.startsWith("image/");
    }
}
