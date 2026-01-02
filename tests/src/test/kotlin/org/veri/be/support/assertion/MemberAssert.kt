package org.veri.be.support.assertion

import org.assertj.core.api.Assertions.assertThat
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType

class MemberAssert private constructor(
    private val actual: Member
) {
    companion object {
        fun assertThat(actual: Member): MemberAssert {
            return MemberAssert(actual)
        }
    }

    fun hasId(expected: Long): MemberAssert {
        assertThat(actual.id).isEqualTo(expected)
        return this
    }

    fun hasEmail(expected: String): MemberAssert {
        assertThat(actual.email).isEqualTo(expected)
        return this
    }

    fun hasNickname(expected: String): MemberAssert {
        assertThat(actual.nickname).isEqualTo(expected)
        return this
    }

    fun hasProfileImageUrl(expected: String): MemberAssert {
        assertThat(actual.profileImageUrl).isEqualTo(expected)
        return this
    }

    fun hasProviderType(expected: ProviderType): MemberAssert {
        assertThat(actual.providerType).isEqualTo(expected)
        return this
    }
}
