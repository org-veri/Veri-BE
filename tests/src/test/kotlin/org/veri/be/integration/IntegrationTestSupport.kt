package org.veri.be.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import org.veri.be.Application
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.service.MemberRepository
import org.veri.be.lib.auth.context.MemberContext

@Transactional
@SpringBootTest(classes = [Application::class])
@Import(SharedTestConfig::class)
abstract class IntegrationTestSupport {

    @Autowired
    protected lateinit var memberRepository: MemberRepository

    @Autowired
    protected lateinit var context: WebApplicationContext

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    protected lateinit var mockMvc: MockMvc

    private lateinit var mockMember: Member

    @BeforeEach
    fun setUpContext() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()

        mockMember = Member.builder()
            .email("smoody@prompt.town")
            .nickname("스무디")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1234")
            .providerType(ProviderType.KAKAO)
            .build()
        mockMember = memberRepository.save(mockMember)
        MemberContext.setCurrentMemberId(mockMember.id)
    }

    fun getMockMember(): Member = mockMember

    @AfterEach
    fun tearDownContext() {
        MemberContext.clear()
    }
}
