package org.veri.be.global.storage.dto;

import java.util.Map;

public record PresignedPostFormResponse(
        String url,
        Map<String, String> fields
) {
}
