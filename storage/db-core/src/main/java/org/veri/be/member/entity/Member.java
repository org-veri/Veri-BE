package org.veri.be.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.veri.be.member.entity.enums.ProviderType;
import org.veri.be.global.entity.Authorizable;
import org.veri.be.global.entity.BaseEntity;

@Getter
@SuperBuilder
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends BaseEntity implements Authorizable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "image", nullable = false, columnDefinition = "VARCHAR(2083)")
    private String profileImageUrl;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type")
    private ProviderType providerType;

    public void updateInfo(String nickname, String profileImageUrl) {
        this.nickname = nickname != null ? nickname : this.nickname;
        this.profileImageUrl = profileImageUrl != null ? profileImageUrl : this.profileImageUrl;
    }

    @Override
    public boolean authorizeMember(Long memberId) {
        return this.id.equals(memberId);
    }
}
