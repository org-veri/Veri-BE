package org.veri.be.unit.global.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.veri.be.global.storage.service.StorageConstants;

class StorageConstantsTest {

    @Test
    @DisplayName("MB 상수는 1MB 바이트 수다")
    void hasMegabyteConstant() {
        assertThat(StorageConstants.MB).isEqualTo(1024 * 1024L);
    }

    @Test
    @DisplayName("인스턴스를 생성할 수 있다")
    void canInstantiate() {
        assertThat(new StorageConstants()).isNotNull();
    }
}
