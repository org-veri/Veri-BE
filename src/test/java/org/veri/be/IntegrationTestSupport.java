package org.veri.be;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.context.MemberContext;

@Transactional
@SpringBootTest
public abstract class IntegrationTestSupport {

    @Autowired
    protected MemberRepository memberRepository;

    Member mockMember;

    @BeforeEach
    void setUpContext() {
        mockMember = Member.builder()
                .email("smoody@prompt.town")
                .nickname("스무디")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1234")
                .providerType(ProviderType.KAKAO)
                .build();
        mockMember = memberRepository.save(mockMember);
        MemberContext.setCurrentMember(mockMember);
    }

    public Member getMockMember() {
        return mockMember;
    }

    @AfterEach
    void tearDownContext() {
        MemberContext.clear();
    }
}
