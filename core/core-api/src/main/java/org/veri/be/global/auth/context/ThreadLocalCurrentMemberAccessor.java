package org.veri.be.global.auth.context;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    private final MemberRepository memberRepository;

    private final Cache<Long, Member> memberCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(1000)
            .build();

    @Override
    public Optional<Member> getCurrentMember() {
        Optional<Member> cachedMember = MemberContext.getCurrentMember();
        if (cachedMember.isPresent()) {
            return cachedMember;
        }

        return MemberContext.getCurrentMemberId()
                .flatMap(memberId -> {
                    // 1차: Caffeine 캐시에서 조회
                    Member cachedMemberInCache = memberCache.getIfPresent(memberId);
                    if (cachedMemberInCache != null) {
                        MemberContext.setCurrentMember(cachedMemberInCache);
                        return Optional.of(cachedMemberInCache);
                    }

                    // 2차: DB 조회 후 캐시에 저장
                    Optional<Member> member = memberRepository.findById(memberId);
                    member.ifPresent(foundMember -> {
                        MemberContext.setCurrentMember(foundMember);
                        memberCache.put(memberId, foundMember);
                    });
                    return member;
                });
    }
}
