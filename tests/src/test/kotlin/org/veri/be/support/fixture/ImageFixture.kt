package org.veri.be.support.fixture

import org.veri.be.domain.image.entity.Image

object ImageFixture {
    fun anImage(): Image.ImageBuilder<*, *> {
        return Image.builder()
            .member(MemberFixture.aMember().build())
            .imageUrl("https://example.com/image.png")
    }
}
