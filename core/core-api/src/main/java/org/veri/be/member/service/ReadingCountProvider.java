package org.veri.be.member.service;

public interface ReadingCountProvider {

    long countReadingsByMemberId(Long memberId);
}
