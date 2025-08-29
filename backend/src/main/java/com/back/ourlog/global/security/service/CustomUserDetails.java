package com.back.ourlog.global.security.service;

import com.back.ourlog.domain.user.entity.Role;
import com.back.ourlog.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    // controller에서 .id로 접근하기 위해 public으로 임시로 변경 했습니다 - 노희철
    public final Integer id;
    private final String email;
    private final String nickname;
    private final Role role;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.authorities = List.of(new SimpleGrantedAuthority(user.getRole().getKey()));
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return null; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}


