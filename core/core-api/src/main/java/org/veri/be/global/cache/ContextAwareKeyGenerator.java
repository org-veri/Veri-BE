package org.veri.be.global.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("contextKeyGenerator")
public class ContextAwareKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (target instanceof ContextKeyProvider provider) {
            Object contextKey = provider.getContextKey();
            if (contextKey == null) {
                return SimpleKey.EMPTY;
            }
            return contextKey;
        }

        if (params.length == 0) {
            throw new IllegalStateException(
                    "@Cacheable 대상이 ContextKeyProvider를 구현하지 않았습니다."
            );
        }

        return new SimpleKey(params);
    }
}
