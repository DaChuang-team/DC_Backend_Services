package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;

import java.time.LocalDateTime;

@Service
public class UserService implements IUserServices {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 注册用户
    @Override
    public void registerUser(UserDTO user) {
        if (userRepository.findByUserName(user.getUserName()) != null) {
            throw new IllegalArgumentException("用户名: " + user.getUserName() + " 已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        //System.out.println("****US-TEST**** Encoded password: " + encodedPassword);
        User_General newUser = new User_General();
        BeanUtils.copyProperties(user, newUser, "userPassword"); // 复制除 userPassword 外的字段
        newUser.setUserPassword(encodedPassword);
        newUser.setUserPermissions(0);
        newUser.setCreateTime(now);
        //处理userGender：若DTO中为null，则设为'U'，否则设为JSON传入值
        newUser.setUserGender(user.getUserGender() != null ? user.getUserGender() : 'U');
        //System.out.println("****US-TEST**** New User: " + newUser.toString());
        userRepository.save(newUser);
    }

    // 验证用户登录
    @Override
    public boolean authenticateUser(String userName, String rawPassword) {
        User_General user = userRepository.findByUserName(userName);
        if(user != null && passwordEncoder.matches(rawPassword, user.getUserPassword())){
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}