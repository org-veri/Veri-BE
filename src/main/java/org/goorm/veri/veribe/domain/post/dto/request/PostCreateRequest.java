package org.goorm.veri.veribe.domain.post.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record PostCreateRequest(
        @NotNull
        @Length(min = 1, max = 50)
        String title,

        String content,

        List<String> imageUrl
//        Long memberBookId,
//        Boolean isPublic
) {
}
