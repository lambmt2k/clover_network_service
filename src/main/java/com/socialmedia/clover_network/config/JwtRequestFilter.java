package com.socialmedia.clover_network.config;

import com.socialmedia.clover_network.dto.res.ApiResponse;
import com.socialmedia.clover_network.repository.UserInfoRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestTokenHeader = request.getHeader("authorization");
        String user_id = null;
        String jwtToken = null;
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("USER_ROLE"));
        if(requestTokenHeader!=null&&requestTokenHeader.startsWith("Bearer")){
            jwtToken = requestTokenHeader.substring(7); // token nam sau "BEARER "

            try{
                user_id = jwtTokenUtil.getUserIDFromToken(jwtToken);
                String reftokent = "";
                if(!jwtToken.equals(reftokent)){
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setCode("");
                    apiResponse.setMessage("");
                    apiResponse.setStatus(HttpStatus.FORBIDDEN.value());
                    logger.info("=================end authenticate badrequest=================");
                }
            }catch(IllegalArgumentException e){
                logger.info("Unable to get JWT token");
            }catch(ExpiredJwtException e){
                logger.info("JWT token has expired");
            }
        }else{
            logger.info("Header authorization not start with 'Bearer' string");
        }

        // sau khi lay va kiem tra JWT token thanh cong
        if(user_id != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtTokenUtil.validateToken(jwtToken,user_id)){
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user_id,null,grantedAuthorities);
            usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            //sau khi setting authetication trong context , pass security config
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }

        filterChain.doFilter(request,response);
    }
}
