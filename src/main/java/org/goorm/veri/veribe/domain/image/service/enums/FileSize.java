package org.goorm.veri.veribe.domain.image.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileSize {
    PERMITTED_SIZE(5_242_880L);

    private final Long size;
}
