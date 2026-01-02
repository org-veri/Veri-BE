package org.veri.be.unit.global.storage

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.veri.be.global.storage.service.StorageConstants
import java.lang.reflect.Modifier

class StorageConstantsTest {

    @Test
    @DisplayName("MB 상수를 조회하면 → 1MB 바이트 수다")
    fun hasMegabyteConstant() {
        assertThat(1024 * 1024L).isEqualTo(StorageConstants.MB)
    }

    @Test
    @DisplayName("생성자를 조회하면 → private이어야 한다")
    fun constructorShouldBePrivate() {
        val constructor = StorageConstants::class.java.getDeclaredConstructor()

        assertThat(Modifier.isPrivate(constructor.modifiers)).isTrue()
    }
}
