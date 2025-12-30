package org.veri.be.member.service;

public interface CardCountProvider {

    long countCardsByMemberId(Long memberId);
}
