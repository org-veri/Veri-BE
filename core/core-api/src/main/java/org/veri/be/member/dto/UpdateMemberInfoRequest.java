package org.veri.be.member.dto;

import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.URL;

public record UpdateMemberInfoRequest(
        @NotEmpty
        String nickname,

        @URL
        String profileImageUrl
) {
}
