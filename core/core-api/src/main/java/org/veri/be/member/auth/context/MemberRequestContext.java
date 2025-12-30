package org.veri.be.member.auth.context;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.member.entity.Member;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberRequestContext {

    private static final ThreadLocal<Member> currentMember = new ThreadLocal<>();

    public static void setCurrentMember(Member member) {
        currentMember.set(member);
        log.debug("Member cached for request");
    }

    public static Optional<Member> getCurrentMember() {
        return Optional.ofNullable(currentMember.get());
    }

    public static void clear() {
        currentMember.remove();
        log.debug("Member cache cleared");
    }
}
