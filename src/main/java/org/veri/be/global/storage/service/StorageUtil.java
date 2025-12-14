package org.veri.be.global.storage.service;

public final class StorageUtil {

    public static boolean isImage(String contentType) {
        return contentType.startsWith("image/");
    }
}
