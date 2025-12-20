package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    private final MemberRepository memberRepository;

    @Override
    public Optional<Member> getCurrentMember() {
        Optional<Member> cachedMember = MemberContext.getCurrentMember();
        if (cachedMember.isPresent()) {
            return cachedMember;
        }

        return MemberContext.getCurrentMemberId()
                .flatMap(memberId -> {
                    Optional<Member> member = memberRepository.findById(memberId);
                    member.ifPresent(MemberContext::setCurrentMember);
                    return member;
                });
    }
}
