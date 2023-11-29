package com.socialmedia.clover_network.config;

import com.socialmedia.clover_network.entity.TokenItem;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public abstract class TokenAuthenticationFilter extends OncePerRequestFilter {
    protected abstract TokenItem getTokenItem(String tokenId);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String tokenId = getTokenId(request);

        if (tokenId != null) {
            TokenItem tokenItem = getTokenItem(tokenId);

            if (tokenItem == null || (tokenItem.isValidTokenItem()) && tokenItem.isDelFlag()) {
                sendUnauthorized(response);
                return;
            }

            AuthenticationUser user = new AuthenticationUser(tokenItem.getUserId(), tokenItem.getUserId(), true);
            SecurityContextHolder.getContext().setAuthentication(user);

        }
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    private String getTokenId(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.startsWithIgnoreCase(bearerToken, "Bearer ")) {
            return bearerToken.substring(7);
        }

        return request.getParameter("token");
    }
}
