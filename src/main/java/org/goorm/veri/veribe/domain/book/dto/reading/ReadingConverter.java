package org.goorm.veri.veribe.domain.book.dto.reading;

import org.goorm.veri.veribe.domain.auth.service.AuthUtil;
import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingDetailResponse;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;

import java.util.List;

public class ReadingConverter {

    public static ReadingDetailResponse toReadingDetailResponse(Reading reading) {

        List<ReadingDetailResponse.CardSummaryResponse> summaries = reading.getCards().stream()
                .map(card -> new ReadingDetailResponse.CardSummaryResponse(card.getId(), card.getImage(), card.getIsPublic()))
                .toList();

        // 독서 상세 조회시 본인 외에는 공개된 카드 요약 정보만 노출
        // Todo. 엔드포인트 분리의 경우 프론트 수정 필요
        if (!reading.getMember().getId().equals(AuthUtil.getCurrentMember().getId())) {
            summaries = summaries.stream()
                    .filter(ReadingDetailResponse.CardSummaryResponse::isPublic)
                    .toList();
        }

        return ReadingDetailResponse.builder()
                .memberBookId(reading.getId())
                .member(MemberProfile.from(reading.getMember()))
                .title(reading.getBook().getTitle())
                .imageUrl(reading.getBook().getImage())
                .status(reading.getStatus())
                .score(reading.getScore())
                .author(reading.getBook().getAuthor())
                .startedAt(reading.getStartedAt())
                .endedAt(reading.getEndedAt())
                .cardSummaries(summaries)
                .build();
    }
}
