package org.veri.be.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.card.service.CardRepository;
import org.veri.be.member.service.CardCountProvider;

@Service
@RequiredArgsConstructor
public class CardCountProviderService implements CardCountProvider {

    private final CardRepository cardRepository;

    @Override
    public long countCardsByMemberId(Long memberId) {
        return cardRepository.countAllByMemberId(memberId);
    }
}
