package com.example.blog.services;


import com.example.blog.models.UserInfo;
import com.example.blog.repo.UserInfoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.blog.models.User;
import com.example.blog.repo.UserRepository;

import java.util.Optional;

@Service
public class UserInfoService {
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfoService(UserRepository userRepository, UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> userByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> userById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserInfo> infoByUserID(Long id){return userInfoRepository.findByUserId(id);}

    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
    }

}
