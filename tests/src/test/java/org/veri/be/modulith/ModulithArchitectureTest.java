package org.veri.be.modulith;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.veri.be.Application;

@ApplicationModuleTest
class ModulithArchitectureTest {

    @Test
    void verifiesModularity() {
        ApplicationModules.of(Application.class).verify();
    }
}
