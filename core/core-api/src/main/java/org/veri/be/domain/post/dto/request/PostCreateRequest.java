package org.veri.be.domain.post.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record PostCreateRequest(
        @NotNull
        @Length(min = 1, max = 50)
        String title,

        @NotNull
        String content,

        @NotNull
        @Size(max = 10)
        List<String> images,

        Long bookId
) {
}
