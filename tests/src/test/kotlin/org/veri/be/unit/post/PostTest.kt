package org.veri.be.unit.post

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.post.entity.Post

class PostTest {

    @Nested
    @DisplayName("addImage")
    inner class AddImage {

        @Test
        @DisplayName("이미지를 추가하면 순서대로 저장된다")
        fun addsImageInOrder() {
            val post = Post.builder()
                .author(member(1L, "author@test.com", "author"))
                .title("title")
                .content("content")
                .build()

            post.addImage("https://example.com/1.png", 1)
            post.addImage("https://example.com/2.png", 2)

            assertThat(post.images).hasSize(2)
            assertThat(post.images[0].displayOrder).isEqualTo(1L)
            assertThat(post.images[1].displayOrder).isEqualTo(2L)
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    inner class AuthorizeMember {

        @Test
        @DisplayName("작성자가 아니면 false를 반환한다")
        fun falseWhenNotOwner() {
            val author = member(1L, "author@test.com", "author")
            val post = Post.builder()
                .author(author)
                .title("title")
                .content("content")
                .build()

            assertThat(post.authorizeMember(2L)).isFalse()
        }
    }

    @Nested
    @DisplayName("publish/unpublish")
    inner class PublishUnpublish {

        @Test
        @DisplayName("작성자가 공개 상태로 변경한다")
        fun publishes() {
            val author = member(1L, "author@test.com", "author")
            val post = Post.builder()
                .author(author)
                .isPublic(false)
                .title("title")
                .content("content")
                .build()

            post.publishBy(author)

            assertThat(post.isPublic).isTrue()
        }

        @Test
        @DisplayName("작성자가 비공개 상태로 변경한다")
        fun unpublishes() {
            val author = member(1L, "author@test.com", "author")
            val post = Post.builder()
                .author(author)
                .isPublic(true)
                .title("title")
                .content("content")
                .build()

            post.unpublishBy(author)

            assertThat(post.isPublic).isFalse()
        }
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
