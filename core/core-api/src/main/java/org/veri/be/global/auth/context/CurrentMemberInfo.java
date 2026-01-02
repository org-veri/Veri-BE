package org.veri.be.global.auth.context;

import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;

public record CurrentMemberInfo(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String providerId,
        ProviderType providerType
) {
    public static CurrentMemberInfo from(Member member) {
        return new CurrentMemberInfo(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl(),
                member.getProviderId(),
                member.getProviderType()
        );
    }
}
