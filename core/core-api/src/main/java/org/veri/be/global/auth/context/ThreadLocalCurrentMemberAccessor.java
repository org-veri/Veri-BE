package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.cache.CacheConfig;
import org.veri.be.global.cache.ContextKeyProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor, ContextKeyProvider {

    private final MemberRepository memberRepository;

    @Override
    public Optional<CurrentMemberInfo> getCurrentMemberInfo() {
        return Optional.ofNullable(getCurrentMemberInfoOrNull());
    }

    @Override
    @Cacheable(
            cacheNames = CacheConfig.CURRENT_MEMBER_INFO,
            keyGenerator = "contextKeyGenerator",
            unless = "#result == null"
    )
    public CurrentMemberInfo getCurrentMemberInfoOrNull() {
        return MemberContext.getCurrentMemberId()
                .flatMap(memberRepository::findById)
                .map(CurrentMemberInfo::from)
                .orElse(null);
    }

    @Override
    public Optional<Member> getCurrentMember() {
        return MemberContext.getCurrentMemberId()
                .flatMap(memberRepository::findById);
    }

    @Override
    public Object getContextKey() {
        return MemberContext.getCurrentMemberId()
                .map(id -> (Object) id)
                .orElse("ANONYMOUS");
    }
}
