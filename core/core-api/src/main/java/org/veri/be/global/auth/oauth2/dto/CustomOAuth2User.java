package org.veri.be.global.auth.oauth2.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final String providerType;

    /**
     * Constructs a {@code CustomOAuth2User} using the provided parameters.
     *
     * @param authorities      the authorities granted to the user
     * @param attributes       the attributes about the user
     * @param nameAttributeKey the key used to access the user's &quot;name&quot; from the
     *                         {@link #getAttributes()}
     * @param registrationId   the registration ID of the provider
     */
    public CustomOAuth2User(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            String registrationId
    ) {

        super(authorities, attributes, nameAttributeKey);
        this.providerType = registrationId.toUpperCase();
    }
}
