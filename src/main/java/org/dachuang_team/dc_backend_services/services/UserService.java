package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;

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
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        //System.out.println("****US-TEST**** Encoded password: " + encodedPassword);
        User_General newUser = new User_General();
        newUser.setUserName(user.getUserName());
        newUser.setUserPhone(user.getUserPhone());
        newUser.setUserPassword(encodedPassword);
        newUser.setUserPermissions(0);
        //System.out.println("****US-TEST**** New User: " + newUser.toString());
        userRepository.save(newUser);
    }

    // 验证用户登录
    @Override
    public boolean authenticateUser(String userName, String rawPassword) {
        User_General user = userRepository.findByUserName(userName);
        return user != null && passwordEncoder.matches(rawPassword, user.getUserPassword());
    }
}