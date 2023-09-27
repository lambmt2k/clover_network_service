package com.socialmedia.clover_network.config;

import com.socialmedia.clover_network.entity.TokenItem;
import com.socialmedia.clover_network.service.AuthenticationService;
import org.springframework.stereotype.Component;

/**
 * @author dathn
 */
@Component
public class TokenAuthenticationFilterImpl extends TokenAuthenticationFilter {

    private final AuthenticationService authenticationService;

    public TokenAuthenticationFilterImpl(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    protected TokenItem getTokenItem(String tokenId) {
        return authenticationService.getTokenItem(tokenId);
    }

}
