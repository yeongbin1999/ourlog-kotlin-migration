package com.back.ourlog.domain.user.service;

import com.back.ourlog.domain.user.dto.MyProfileResponse;
import com.back.ourlog.domain.user.dto.UserProfileResponse;
import com.back.ourlog.domain.user.entity.User;
import com.back.ourlog.domain.user.repository.UserRepository;
import com.back.ourlog.global.exception.CustomException;
import com.back.ourlog.global.exception.ErrorCode;
import com.back.ourlog.global.security.oauth.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    
    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public User registerOrGetOAuthUser(OAuthAttributes attributes) {
        return userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseGet(() -> registerOAuthUser(attributes));
    }

    private User registerOAuthUser(OAuthAttributes attributes) {
        User user = User.builder()
                .email(attributes.getEmail())
                .nickname(attributes.getName())
                .provider(attributes.getProvider())
                .providerId(attributes.getProviderId())
                .profileImageUrl(attributes.getAttributes().get("profile_image") != null ?
                        attributes.getAttributes().get("profile_image").toString() : null)
                .build();

        return userRepository.save(user);
    }

    public MyProfileResponse getMyProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return MyProfileResponse.from(user);
    }
    
    public UserProfileResponse getUserProfile(Integer userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.from(user);
    }

    public Page<UserProfileResponse> searchUsersByNickname(String keyword, Pageable pageable) {
        Page<User> users = userRepository.findByNicknameContainingIgnoreCase(keyword, pageable);
        return users.map(UserProfileResponse::from);
    }
}
