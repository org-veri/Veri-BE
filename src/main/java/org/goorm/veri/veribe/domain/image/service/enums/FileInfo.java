package org.goorm.veri.veribe.domain.image.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileInfo {
    BASE_META_PATH("/Users/Jinyoung/ocr/tessdata"),
    SUPPORT_LANGUAGE("kor+eng");

    private final String value;
}
