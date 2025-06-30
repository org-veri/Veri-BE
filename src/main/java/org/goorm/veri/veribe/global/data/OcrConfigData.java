package org.goorm.veri.veribe.global.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ocr")
public class OcrConfigData {
    private String tessdataPath;
    private String language;
}