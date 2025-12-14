package org.veri.be.global.auth.context;

import org.springframework.stereotype.Component;
import org.veri.be.domain.member.entity.Member;

import java.util.Optional;

@Component
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    @Override
    public Optional<Member> getCurrentMember() {
        return MemberContext.getCurrentMember();
    }
}
