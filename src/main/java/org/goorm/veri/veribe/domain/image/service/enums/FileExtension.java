package org.goorm.veri.veribe.domain.image.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileExtension {
    JPG(".jpg"),
    JPEG(".jpeg"),
    PNG(".png");

    private final String extension;

    public static boolean checkAvailable(String fileName){
        String lowerFileName = fileName.toLowerCase();
        for (FileExtension value : FileExtension.values()) {
            if (lowerFileName.endsWith(value.getExtension())){
                return true;
            }
        }
        return false;
    }

}
