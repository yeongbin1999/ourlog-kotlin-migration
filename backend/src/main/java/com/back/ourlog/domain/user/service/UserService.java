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
        // profileImageUrl 값을 미리 변수로 추출하여 가독성을 높입니다.
        Object profileImageObj = attributes.getAttributes().get("profile_image");
        String profileImageUrl = (profileImageObj != null) ? profileImageObj.toString() : null;

        // User.Companion.createSocialUser를 호출하고 파라미터를 순서에 맞게 전달합니다.
        User user = User.Companion.createSocialUser(
                attributes.getProvider(),
                attributes.getProviderId(),
                attributes.getEmail(),
                attributes.getName(),
                profileImageUrl
        );

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
