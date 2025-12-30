package org.veri.be.image.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "ocr_result")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ocr_result_id")
    private Long id;

    @Column(name = "image_url", nullable = false, columnDefinition = "VARCHAR(2083)")
    private String imageUrl;

    @Column(name = "preProcessed_url", nullable = true, columnDefinition = "VARCHAR(2083)")
    private String preProcessedUrl;

    @Column(name = "result_text", nullable = false, columnDefinition = "TEXT")
    private String resultText;

    private String ocrService;
}
