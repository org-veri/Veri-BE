package org.veri.be.member.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.member.entity.Member;
import org.veri.be.member.repository.MemberRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    private final MemberRepository memberRepository;

    @Override
    public Optional<Member> getCurrentMember() {
        Optional<Member> cachedMember = MemberRequestContext.getCurrentMember();
        if (cachedMember.isPresent()) {
            return cachedMember;
        }

        return MemberContext.getCurrentMemberId()
                .flatMap(memberId -> {
                    if (memberRepository == null) {
                        return Optional.empty();
                    }
                    Optional<Member> member = memberRepository.findById(memberId);
                    member.ifPresent(MemberRequestContext::setCurrentMember);
                    return member;
                });
    }
}
