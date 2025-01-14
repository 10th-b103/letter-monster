package com.lemon.backend.global.auth;

import com.lemon.backend.domain.users.user.entity.Role;
import com.lemon.backend.domain.users.user.entity.Users;
import com.lemon.backend.domain.users.user.repository.UserRepository;
import com.lemon.backend.domain.users.user.service.UserService;
import com.lemon.backend.global.cookie.CookieUtil;
import com.lemon.backend.global.jwt.JwtTokenProvider;
import com.lemon.backend.global.jwt.TokenResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static com.lemon.backend.global.auth.OAuth2AuthorizationRequestBasedOnCookieRepository.FIREBASE_TOKEN_COOKIE_NAME;
import static com.lemon.backend.global.auth.OAuth2AuthorizationRequestBasedOnCookieRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<String> cookieRedirectUrl = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
        log.info("Authentication Success");
        Optional<String> fireBaseToken = CookieUtil.getCookie(request, FIREBASE_TOKEN_COOKIE_NAME).map(Cookie::getValue);

        if (cookieRedirectUrl.isPresent()) {
            Integer userId = Integer.valueOf(userPrincipal.getName());

            String accessToken = jwtTokenProvider.createAccessToken(userId, Role.ROLE_USER.name());
            String refreshToken = jwtTokenProvider.createRefreshToken();
            userService.saveRefreshTokenIntoRedis(userId, refreshToken);

            String redirectUrl = UriComponentsBuilder.fromUriString(cookieRedirectUrl.get())
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            if (fireBaseToken.isPresent() && !fireBaseToken.get().isEmpty()) {
                userRepository.updateFirebaseToken(userId, fireBaseToken.get());
            }
            redirectStrategy.sendRedirect(request, response, redirectUrl);
        }
    }
}