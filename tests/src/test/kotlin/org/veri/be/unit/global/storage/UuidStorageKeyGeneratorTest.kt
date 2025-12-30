package org.veri.be.unit.global.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.storage.service.UuidStorageKeyGenerator
import java.util.UUID

class UuidStorageKeyGeneratorTest {

    @Nested
    @DisplayName("generate")
    inner class Generate {

        @Test
        @DisplayName("prefix와 확장자를 포함한 키를 생성한다")
        fun generatesKeyWithPrefixAndExtension() {
            val generator = UuidStorageKeyGenerator()

            val key = generator.generate("image/png", "public")

            assertThat(key)
                .startsWith("public/")
                .endsWith(".png")
            assertThat(extractUuid(key)).isNotNull()
        }

        private fun extractUuid(key: String): UUID {
            val withoutPrefix = key.substring("public/".length)
            val uuidPart = withoutPrefix.substring(0, withoutPrefix.lastIndexOf('.'))
            return UUID.fromString(uuidPart)
        }
    }
}
