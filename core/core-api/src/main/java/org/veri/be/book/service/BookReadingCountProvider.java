package org.veri.be.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.service.ReadingRepository;
import org.veri.be.member.service.ReadingCountProvider;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class BookReadingCountProvider implements ReadingCountProvider {

    private final ReadingRepository readingRepository;

    @Override
    public long countReadingsByMemberId(Long memberId) {
        return readingRepository.countAllByMemberId(memberId);
    }
}
