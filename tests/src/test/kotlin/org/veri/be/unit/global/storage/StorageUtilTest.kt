package org.veri.be.unit.global.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.storage.service.StorageUtil

class StorageUtilTest {

    @Nested
    @DisplayName("isImage")
    inner class IsImage {

        @Test
        @DisplayName("image 타입이면 true를 반환한다")
        fun returnsTrueForImage() {
            assertThat(StorageUtil.isImage("image/png")).isTrue()
        }

        @Test
        @DisplayName("image 타입이 아니면 false를 반환한다")
        fun returnsFalseForNonImage() {
            assertThat(StorageUtil.isImage("application/pdf")).isFalse()
        }
    }
}
