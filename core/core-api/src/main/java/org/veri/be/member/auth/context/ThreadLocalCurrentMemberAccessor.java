package org.veri.be.member.auth.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.lib.auth.context.MemberContext;
import org.veri.be.member.entity.Member;
import org.veri.be.member.service.MemberQueryService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ThreadLocalCurrentMemberAccessor implements CurrentMemberAccessor {

    private final MemberQueryService memberQueryService;

    @Override
    public Optional<Member> getCurrentMember() {
        Optional<Member> cachedMember = MemberRequestContext.getCurrentMember();
        if (cachedMember.isPresent()) {
            return cachedMember;
        }

        return MemberContext.getCurrentMemberId()
                .flatMap(memberId -> memberQueryService.findOptionalById(memberId)
                        .map(member -> {
                            MemberRequestContext.setCurrentMember(member);
                            return member;
                        }));
    }
}
