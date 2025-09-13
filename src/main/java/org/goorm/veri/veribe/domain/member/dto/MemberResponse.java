package org.goorm.veri.veribe.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import org.goorm.veri.veribe.domain.member.entity.Member;

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

    @Getter
    @Builder
    public static class MemberSimpleResponse {
        private Long id;
        private String nickname;
        private String image;

        public static MemberSimpleResponse from(Member member) {
            return MemberSimpleResponse.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .image(member.getProfileImageUrl())
                    .build();
        }
    }
}
