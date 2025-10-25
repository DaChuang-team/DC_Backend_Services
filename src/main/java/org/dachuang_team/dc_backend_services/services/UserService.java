package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.userUpdateDTO;

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
        User_General newUser = new User_General();
        BeanUtils.copyProperties(user, newUser, "userPassword"); // 复制除 userPassword 外的字段
        newUser.setUserPassword(encodedPassword);
        newUser.setUserPermissions(0);
        newUser.setCreateTime(now);
        //处理userGender：若DTO中为null，则设为'U'，否则设为JSON传入值
        newUser.setUserGender(user.getUserGender() != null ? user.getUserGender() : 'U');
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

    @Override
    public User_General getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public boolean updateInfo(String userName, userUpdateDTO userUpdateDTO) {
        User_General user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userName);
        }

        if (!passwordEncoder.matches(userUpdateDTO.getOldPassword(), user.getUserPassword())) {
            throw new IllegalArgumentException("用户名或密码不正确");
        }

        // 更新密码,需要验证旧密码
        if (userUpdateDTO.isNeedPasswordChange()) {
            if (userUpdateDTO.getUserPassword() == null || userUpdateDTO.getUserPassword().isEmpty()) {
                throw new IllegalArgumentException("需要提供新密码以修改密码");
            }
            String encodedPassword = passwordEncoder.encode(userUpdateDTO.getUserPassword());
            user.setUserPassword(encodedPassword);
        }

        // 更新用户名，需检查新用户名是否已存在
        if (userUpdateDTO.getUserName() != null && !userUpdateDTO.getUserName().equals(userName)) {
            if (userRepository.findByUserName(userUpdateDTO.getUserName()) != null) {
                throw new IllegalArgumentException("用户名: " + userUpdateDTO.getUserName() + " 已存在");
            }
            user.setUserName(userUpdateDTO.getUserName());
        }

        if (userUpdateDTO.getUserPhone() != null) {
            user.setUserPhone(userUpdateDTO.getUserPhone());
        }
        if (userUpdateDTO.getUserPreference() != null) {
            user.setUserPreference(userUpdateDTO.getUserPreference());
        }
        if (userUpdateDTO.getUserGender() != null) {
            user.setUserGender(userUpdateDTO.getUserGender());
        }
        if (userUpdateDTO.getUserAvatarURL() != null) {
            user.setUserAvatarURL(userUpdateDTO.getUserAvatarURL());
        }
        if (userUpdateDTO.getUserBirthday() != null) {
            user.setUserBirthday(userUpdateDTO.getUserBirthday());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        System.out.println("<US-UPD-TSET> User info updated for userName: " + userUpdateDTO.toString() );
        return true;
    }
}