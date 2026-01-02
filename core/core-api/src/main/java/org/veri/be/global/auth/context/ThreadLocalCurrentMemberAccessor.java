package org.veri.be.global.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    private final MemberRepository memberRepository;

    @Override
    public CurrentMemberInfo getCurrentMemberInfoOrNull() {
        return MemberContext.getCurrentMemberInfo().orElse(null);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Member> getCurrentMember() {
        return memberRepository.findById(MemberContext.getCurrentMemberId().orElseThrow(
                () -> ApplicationException.of(AuthErrorInfo.UNAUTHORIZED)
        ));
    }
}
