package org.goorm.veri.veribe.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

public class MemberResponse {

    @Getter
    @Builder
    public static class MemberInfoResponse {
        private String email;
        private String nickname;
        private String image;
        private Integer numOfReadBook;
        private Integer numOfCard;
    }
}
