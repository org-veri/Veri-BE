package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.global.cache.CacheConfig;
import org.veri.be.global.cache.ContextKeyProvider;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor, ContextKeyProvider {

    private final MemberRepository memberRepository;

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

    @Transactional(readOnly = true)
    @Override
    public Optional<Member> getCurrentMember() {
        return memberRepository.findById(MemberContext.getCurrentMemberId().orElseThrow(
                () -> ApplicationException.of(AuthErrorInfo.UNAUTHORIZED)
        ));
    }

    @Override
    public Object getContextKey() {
        return MemberContext.getCurrentMemberId()
                .map(id -> (Object) id)
                .orElse("ANONYMOUS");
    }
}
