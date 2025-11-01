package org.veri.be.global.auth.context;

import org.veri.be.domain.member.entity.Member;

import java.util.Optional;

public class MemberContext {

    private static final ThreadLocal<Member> currentMember = new ThreadLocal<>();

    public static void setMember(Member member) {
        currentMember.set(member);
    }

    public static Optional<Member> getMember() {
        return Optional.ofNullable(currentMember.get());
    }

    public static void clear() {
        currentMember.remove();
    }
}
