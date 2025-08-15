package org.goorm.veri.veribe.domain.image.service;

import lombok.extern.slf4j.Slf4j;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorInfo;
import org.goorm.veri.veribe.domain.image.repository.OcrResultRepository;
import org.goorm.veri.veribe.global.exception.http.ExternalApiException;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
@Slf4j
@Service
public class TextractOcrService extends OcrService {

    private final TextractClient textractClient;

    public TextractOcrService(
            OcrResultRepository ocrResultRepository,
            TextractClient textractClient
    ) {
        super(ocrResultRepository);
        this.textractClient = textractClient;
    }

    @Override
    protected String serviceName() {
        return "Textract";
    }

    /**
     * 전처리된 S3 URL로 먼저 Textract OCR을 시도하고, 실패 시 원본 URL로 재시도합니다.
     */
    @Override
    protected String doExtract(String s3Url) {
        String preprocessedS3Url = this.getPreprocessedUrl(s3Url);
        try {
            String text = callTextractAndProcess(preprocessedS3Url);
            saveOcrResult(s3Url, preprocessedS3Url, text);
            return text;
        } catch (Exception e1) {
            log.warn("Textract OCR(preprocessed) 실패: {} -> 원본으로 재시도", e1.getMessage());
            try {
                String text = callTextractAndProcess(s3Url);
                saveOcrResult(s3Url, null, text);
                return text;
            } catch (Exception e2) {
                log.error("Textract OCR 원본도 실패: {}", e2.getMessage());
                throw new ExternalApiException(ImageErrorInfo.OCR_PROCESSING_FAILED);
            }
        }
    }

    /**
     * S3 URL을 사용하여 AWS Textract API를 호출하고, 응답을 파싱하여 텍스트를 반환합니다.
     *
     * @param s3Url S3에 업로드된 이미지 URL
     * @return 추출된 텍스트
     * @throws MalformedURLException, IllegalArgumentException, SdkException URL 파싱 또는 API 호출 실패 시 발생
     */
    private String callTextractAndProcess(String s3Url) throws MalformedURLException {
        // 1. S3 URL 파싱
        S3Location loc = parseS3Url(s3Url);

        // 2. Textract 요청 생성
        Document document = Document.builder()
                .s3Object(S3Object.builder()
                        .bucket(loc.bucket())
                        .name(loc.key())
                        .build())
                .build();
        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder().document(document).build();

        // 3. Textract API 호출
        DetectDocumentTextResponse resp = textractClient.detectDocumentText(request);
        log.debug("Textract response: {}", resp.sdkHttpResponse().statusCode());

        // 4. 응답에서 텍스트 파싱 및 정렬
        return parseTextFromResponse(resp);
    }

    /**
     * Textract 응답에서 텍스트 라인을 추출하고 정렬하여 하나의 문자열로 결합합니다.
     *
     * @param resp Textract API 응답 객체
     * @return 정렬된 전체 텍스트
     */
    private String parseTextFromResponse(DetectDocumentTextResponse resp) {
        List<Block> resultBlocks = resp.blocks();
        return resultBlocks.stream()
                .filter(block -> "LINE".equals(block.blockTypeAsString()))
                .map(Block::text)
                .collect(Collectors.joining("\n"));
    }

    // --- S3 URL 파싱 ---
    private record S3Location(String bucket, String key) {
    }

    private S3Location parseS3Url(String url) throws MalformedURLException {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("S3 URL is blank");

        // s3://bucket/key 형식
        if (url.startsWith("s3://")) {
            String noScheme = url.substring(5);
            int slash = noScheme.indexOf('/');
            if (slash < 1 || slash == noScheme.length() - 1)
                throw new IllegalArgumentException("Invalid s3:// URL: " + url);
            return new S3Location(noScheme.substring(0, slash), noScheme.substring(slash + 1));
        }

        URL u = new URL(url);
        String host = u.getHost();
        String sanitizedPath = u.getPath().startsWith("/") ? u.getPath().substring(1) : u.getPath();

        // 가상 호스팅 스타일: https://bucket.s3.region.amazonaws.com/key
        if (host.matches("^[^.]+\\.s3([.-][a-z0-9-]+)?\\.amazonaws\\.com$")) {
            String bucket = host.substring(0, host.indexOf(".s3"));
            String key = sanitizedPath;
            if (bucket.isEmpty() || key.isEmpty())
                throw new IllegalArgumentException("Invalid virtual-hosted S3 URL: " + url);
            return new S3Location(bucket, key);
        }

        // 경로 스타일: https://s3.region.amazonaws.com/bucket/key
        if (host.matches("^s3([.-][a-z0-9-]+)?\\.amazonaws\\.com$")) {
            int slash = sanitizedPath.indexOf('/');
            if (slash < 1 || slash == sanitizedPath.length() - 1)
                throw new IllegalArgumentException("Invalid path-style S3 URL: " + url);
            return new S3Location(sanitizedPath.substring(0, slash), sanitizedPath.substring(slash + 1));
        }

        throw new IllegalArgumentException("Not a recognized S3 URL: " + url);
    }
}
