package com.back.ourlog.global.security.service;

import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.service.UserService;
import com.back.ourlog.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userService.findByEmail(email);
            return new CustomUserDetails(user);
        } catch (CustomException e) {
            throw new UsernameNotFoundException("User not found: " + email, e);
        }
    }

    public CustomUserDetails loadUserById(String userIdStr) {
        User user = userService.findById(Integer.parseInt(userIdStr));
        return new CustomUserDetails(user);
    }
}
