package org.veri.be.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import org.veri.be.domain.member.entity.Member;

public class MemberResponse {

    @Getter
    @Builder
    public static class MemberInfoResponse {
        private String email;
        private String nickname;
        private String image;
        private Integer numOfReadBook;
        private Integer numOfCard;

        public static MemberInfoResponse from(Member member, int numOfBook, int numOfCard) {
            return MemberInfoResponse.builder()
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .image(member.getProfileImageUrl())
                    .numOfReadBook(numOfBook)
                    .numOfCard(numOfCard)
                    .build();
        }
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
