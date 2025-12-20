package org.veri.be.unit.global.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.global.storage.service.UuidStorageKeyGenerator;

class UuidStorageKeyGeneratorTest {

    @Nested
    @DisplayName("generate")
    class Generate {

        @Test
        @DisplayName("prefix와 확장자를 포함한 키를 생성한다")
        void generatesKeyWithPrefixAndExtension() {
            UuidStorageKeyGenerator generator = new UuidStorageKeyGenerator();

            String key = generator.generate("image/png", "public");

            assertThat(key).startsWith("public/");
            assertThat(key).endsWith(".png");
            assertThat(extractUuid(key)).isNotNull();
        }

        private UUID extractUuid(String key) {
            String withoutPrefix = key.substring("public/".length());
            String uuidPart = withoutPrefix.substring(0, withoutPrefix.lastIndexOf('.'));
            return UUID.fromString(uuidPart);
        }
    }
}
