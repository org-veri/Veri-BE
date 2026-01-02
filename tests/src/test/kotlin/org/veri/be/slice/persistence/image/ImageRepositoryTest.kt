package org.veri.be.slice.persistence.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.veri.be.domain.image.repository.ImageRepository
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport
import org.veri.be.support.fixture.ImageFixture
import org.veri.be.support.fixture.MemberFixture

class ImageRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var imageRepository: ImageRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Nested
    @DisplayName("findByMemberId")
    inner class FindByMemberId {

        @Test
        @DisplayName("회원별 이미지 URL을 조회하면 → 페이징 결과를 반환한다")
        fun returnsMemberImageUrls() {
            val member = saveMember("member@test.com", "member")
            val other = saveMember("other@test.com", "other")
            saveImage(member, "https://example.com/image-1.png")
            saveImage(member, "https://example.com/image-2.png")
            saveImage(member, "https://example.com/image-3.png")
            saveImage(other, "https://example.com/other.png")

            val page = imageRepository.findByMemberId(
                member.id,
                PageRequest.of(0, 2, Sort.by("id").ascending())
            )

            val urls = page.content
            assertThat(page.totalElements).isEqualTo(3)
            assertThat(urls)
                .isNotEmpty
                .allMatch { url -> url.startsWith("https://example.com/image-") }
        }
    }

    private fun saveMember(email: String, nickname: String): org.veri.be.domain.member.entity.Member {
        return memberRepository.save(
            MemberFixture.aMember()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-$nickname")
                .providerType(ProviderType.KAKAO)
                .build()
        )
    }

    private fun saveImage(member: org.veri.be.domain.member.entity.Member, url: String): org.veri.be.domain.image.entity.Image {
        return imageRepository.save(
            ImageFixture.anImage()
                .member(member)
                .imageUrl(url)
                .build()
        )
    }
}
