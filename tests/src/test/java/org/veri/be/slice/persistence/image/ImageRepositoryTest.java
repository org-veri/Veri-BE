package org.veri.be.slice.persistence.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.veri.be.domain.image.entity.Image;
import org.veri.be.domain.image.repository.ImageRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class ImageRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    MemberRepository memberRepository;

    @Nested
    @DisplayName("findByMemberId")
    class FindByMemberId {

        @Test
        @DisplayName("회원별 이미지 URL을 페이징 조회한다")
        void returnsMemberImageUrls() {
            Member member = saveMember("member@test.com", "member");
            Member other = saveMember("other@test.com", "other");
            saveImage(member, "https://example.com/image-1.png");
            saveImage(member, "https://example.com/image-2.png");
            saveImage(member, "https://example.com/image-3.png");
            saveImage(other, "https://example.com/other.png");

            Page<String> page = imageRepository.findByMemberId(
                    member.getId(),
                    PageRequest.of(0, 2, Sort.by("id").ascending())
            );

            List<String> urls = page.getContent();
            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(urls).allMatch(url -> url.startsWith("https://example.com/image-"));
        }
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build());
    }

    private Image saveImage(Member member, String url) {
        return imageRepository.save(Image.builder()
                .member(member)
                .imageUrl(url)
                .build());
    }
}
