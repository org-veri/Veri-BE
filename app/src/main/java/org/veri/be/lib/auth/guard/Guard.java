package org.veri.be.lib.auth.guard;

public interface Guard {
    void canActivate() throws Exception;
}
