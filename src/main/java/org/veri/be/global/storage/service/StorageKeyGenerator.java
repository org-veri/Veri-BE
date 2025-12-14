package org.veri.be.global.storage.service;

public interface StorageKeyGenerator {

    String generate(String contentType, String prefix);
}
