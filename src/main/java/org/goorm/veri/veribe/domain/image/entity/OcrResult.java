package org.goorm.veri.veribe.domain.image.entity;

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

    @Column(name = "result_text", nullable = false, columnDefinition = "TEXT")
    private String resultText;

    private String ocrService;
}
