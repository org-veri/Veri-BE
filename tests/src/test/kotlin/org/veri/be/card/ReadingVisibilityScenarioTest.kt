package org.veri.be.card

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.modulith.test.EnableScenarios
import org.springframework.modulith.test.Scenario
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import org.veri.be.book.event.ReadingVisibilityChangedEvent
import org.veri.be.book.service.BookshelfQueryService
import org.veri.be.card.entity.Card
import org.veri.be.card.service.CardRepository
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.veri.be.global.storage.service.StorageService
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType

@ApplicationModuleTest(extraIncludes = ["org.veri.be.book", "org.veri.be.member"])
@ImportAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataJpaRepositoriesAutoConfiguration::class
    ]
)
@Import(ReadingVisibilityScenarioTest.TestConfig::class)
@EnableScenarios
class ReadingVisibilityScenarioTest @Autowired constructor(
    private val cardRepository: CardRepository
) {

    @Test
    fun `독서 비공개 이벤트로 카드가 비공개 처리된다`(scenario: Scenario) {
        val readingId = 100L
        val card = card(member("member@test.com", "member", "provider-1"))
        given(cardRepository.findAllByReadingId(readingId)).willReturn(listOf(card))

        scenario.publish(ReadingVisibilityChangedEvent(readingId, false))
            .andWaitForStateChange(
                { card.isPublic },
                { isPublic -> !isPublic }
            )
            .andVerify { isPublic ->
                assertThat(isPublic).isFalse()
            }
    }

    private fun member(email: String, nickname: String, providerId: String): Member {
        return Member.builder()
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId(providerId)
            .providerType(ProviderType.KAKAO)
            .build()
    }

    private fun card(member: Member): Card {
        return Card.builder()
            .member(member)
            .content("content")
            .image("https://example.com/card.png")
            .isPublic(true)
            .build()
    }

    @TestConfiguration
    class TestConfig {

        @Bean
        fun cardRepository(): CardRepository {
            return Mockito.mock(CardRepository::class.java)
        }

        @Bean
        fun bookshelfQueryService(): BookshelfQueryService {
            return Mockito.mock(BookshelfQueryService::class.java)
        }

        @Bean
        fun storageService(): StorageService {
            return Mockito.mock(StorageService::class.java)
        }

        @Bean
        fun transactionManager(): PlatformTransactionManager {
            return NoOpTransactionManager()
        }

        @Bean
        fun transactionTemplate(transactionManager: PlatformTransactionManager): TransactionTemplate {
            return TransactionTemplate(transactionManager)
        }

        private class NoOpTransactionManager : PlatformTransactionManager {
            override fun getTransaction(definition: TransactionDefinition?): TransactionStatus {
                return SimpleTransactionStatus()
            }

            override fun commit(status: TransactionStatus) {
                // no-op for scenario testing without a real datasource
            }

            override fun rollback(status: TransactionStatus) {
                // no-op for scenario testing without a real datasource
            }
        }
    }
}
