package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.cache.CacheConfig;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    private final MemberRepository memberRepository;

    @Override
    @Cacheable(
            cacheNames = CacheConfig.CURRENT_MEMBER_INFO,
            key = "T(org.veri.be.global.auth.context.MemberContext).getCurrentMemberId().orElse(null)",
            condition = "T(org.veri.be.global.auth.context.MemberContext).getCurrentMemberId().isPresent()",
            unless = "#result == null"
    )
    public Optional<CurrentMemberInfo> getCurrentMemberInfo() {
        return MemberContext.getCurrentMemberId()
                .flatMap(memberRepository::findById)
                .map(CurrentMemberInfo::from);
    }

    @Override
    public Optional<Member> getCurrentMember() {
        return MemberContext.getCurrentMemberId()
                .flatMap(memberRepository::findById);
    }
}
