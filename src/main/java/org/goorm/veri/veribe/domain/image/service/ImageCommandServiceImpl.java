package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.TextractException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageCommandServiceImpl implements ImageCommandService {

    private final ImageRepository imageRepository;
    private final TextractClient textractClient;

    @Override
    public String processImageOcrAndSave(Member member, String imageUrl) {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            byte[] imageBytes = toByteArray(inputStream);
            insertImageUrl(imageUrl, member);
            return extractTextWithTextract(imageBytes);

        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.BAD_REQUEST);
        } catch (TextractException e) {
            throw new ImageException(ImageErrorCode.OCR_PROCESSING_FAILED);
        }
    }

    private void insertImageUrl(String imageUrl, Member member) {
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();
        imageRepository.save(image);
    }

    /**
     * AWS Textract를 호출하여 이미지 바이트에서 텍스트를 추출합니다.
     *
     * @param imageBytes 이미지의 byte 배열
     * @return 추출된 텍스트
     */
    private String extractTextWithTextract(byte[] imageBytes) {
        SdkBytes sourceBytes = SdkBytes.fromByteArray(imageBytes);
        Document document = Document.builder().bytes(sourceBytes).build();

        DetectDocumentTextRequest detectDocumentTextRequest = DetectDocumentTextRequest.builder()
                .document(document)
                .build();

        // Textract API를 호출하고, 결과에서 'LINE' 타입의 텍스트만 추출하여 반환합니다.
        List<Block> resultBlocks = textractClient.detectDocumentText(detectDocumentTextRequest).blocks();
        return resultBlocks.stream()
                .filter(block -> "LINE".equals(block.blockTypeAsString()))
                .map(Block::text)
                .collect(Collectors.joining("\n"));
    }

    /**
     * InputStream을 byte 배열로 변환하는 유틸리티 메서드입니다.
     *
     * @param inputStream 변환할 InputStream
     * @return byte 배열
     * @throws IOException 읽기/쓰기 예외
     */
    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
}
