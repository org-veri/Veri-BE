package org.veri.be.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.Application;

@Transactional
@SpringBootTest(classes = Application.class)
@Import(SharedTestConfig.class)
public abstract class IntegrationTestSupport {

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvc mockMvc;

    Member mockMember;

    @BeforeEach
    void setUpContext() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

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
