package org.goorm.veri.veribe.domain.image.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;

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

    // Mime 타입은 (image/jpeg)로 저장되어 정규 파일 확장자로 변경해야 함.
    public static String convertMimeToExtension(String mimeType){
        for (FileExtension value : FileExtension.values()) {
            if (mimeType.contains(value.getExtension().substring(1))){
                return value.getExtension().substring(1); // '.' 제거
            }
        }
        throw new ImageException(ImageErrorCode.UNSUPPORTED_TYPE);
    }

}
