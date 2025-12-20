package org.veri.be.unit.global.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.global.storage.service.StorageUtil;

class StorageUtilTest {

    @Nested
    @DisplayName("isImage")
    class IsImage {

        @Test
        @DisplayName("image 타입이면 true를 반환한다")
        void returnsTrueForImage() {
            assertThat(StorageUtil.isImage("image/png")).isTrue();
        }

        @Test
        @DisplayName("image 타입이 아니면 false를 반환한다")
        void returnsFalseForNonImage() {
            assertThat(StorageUtil.isImage("application/pdf")).isFalse();
        }
    }

    @Test
    @DisplayName("인스턴스를 생성할 수 있다")
    void canInstantiate() {
        assertThat(new StorageUtil()).isNotNull();
    }
}
