package com.back.ourlog.global.rq;

import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.service.UserService;
import com.back.ourlog.global.security.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;

@Component
@RequestScope
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest req;
    private final HttpServletResponse resp;
    private final UserService userService;

    public CustomUserDetails getCurrentUserDetails() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof CustomUserDetails)
                .map(principal -> (CustomUserDetails) principal)
                .orElse(null);
    }

    public User getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails == null) {
            return null;
        }
        return userService.findById(userDetails.getId());
    }

    public String getHeader(String name, String defaultValue) {
        return Optional.ofNullable(req.getHeader(name))
                .filter(header -> !header.isBlank())
                .orElse(defaultValue);
    }

    public void setMockUser(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

//    public void setHeader(String name, String value) {
//        if (value == null || value.isBlank()) {
//            resp.setHeader(name, "");
//        } else {
//            resp.setHeader(name, value);
//        }
//    }
//
//    public String getCookieValue(String name, String defaultValue) {
//        return Optional.ofNullable(req.getCookies())
//                .flatMap(cookies -> Arrays.stream(cookies)
//                        .filter(cookie -> cookie.getName().equals(name))
//                        .map(Cookie::getValue)
//                        .filter(value -> !value.isBlank())
//                        .findFirst())
//                .orElse(defaultValue);
//    }
//
//    public void setCookie(String name, String value) {
//        if (value == null) value = "";
//
//        Cookie cookie = new Cookie(name, value);
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//
//        // TODO: 환경변수 등에서 도메인 가져오기
//        cookie.setDomain("localhost");
//
//        // TODO: HTTPS 환경에서만 true로 설정하도록 변경 권장
//        cookie.setSecure(true);
//
//        cookie.setAttribute("SameSite", "Strict");
//
//        if (value.isBlank()) cookie.setMaxAge(0);  // 쿠키 삭제
//        else cookie.setMaxAge(60 * 60 * 24 * 365); // 1년
//
//        resp.addCookie(cookie);
//    }
//
//    public void deleteCookie(String name) {
//        setCookie(name, null);
//    }
//
//    public void sendRedirect(String url) {
//        try {
//            resp.sendRedirect(url);
//        } catch (Exception e) {
//            throw new RuntimeException("Redirect 실패", e);
//        }
//    }
}
