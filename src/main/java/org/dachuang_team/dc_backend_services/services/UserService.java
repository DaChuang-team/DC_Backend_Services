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
import java.util.List;

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
            // 检查用户状态是否正常
            if ("异常".equals(user.getUserStatus())) {
                throw new IllegalArgumentException("该用户状态异常，禁止登录");
            }
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

        // 判断是否涉及敏感修改（修改密码或修改手机号）
        // 逻辑优化：如果传入了新密码字段且不为空，则自动视为需要修改密码，NPC 标志现在作为辅助确认
        boolean isChangingPassword = userUpdateDTO.isNeedPasswordChange() || (userUpdateDTO.getUserPassword() != null && !userUpdateDTO.getUserPassword().isEmpty());
        boolean isChangingPhone = userUpdateDTO.getUserPhone() != null && !userUpdateDTO.getUserPhone().equals(user.getUserPhone());

        // 如果涉及敏感修改，则必须验证旧密码
        if (isChangingPassword || isChangingPhone) {
            if (userUpdateDTO.getOldPassword() == null || !passwordEncoder.matches(userUpdateDTO.getOldPassword(), user.getUserPassword())) {
                throw new IllegalArgumentException("修改密码或手机号需要正确的旧密码验证");
            }
        }

        // 1. 修改密码逻辑
        if (isChangingPassword) {
            if (userUpdateDTO.getUserPassword() == null || userUpdateDTO.getUserPassword().isEmpty()) {
                throw new IllegalArgumentException("需要提供新密码以修改密码");
            }
            String encodedPassword = passwordEncoder.encode(userUpdateDTO.getUserPassword());
            user.setUserPassword(encodedPassword);
        }

        // 2. 修改手机号逻辑 (需验证原手机号)
        if (isChangingPhone) {
            if (userUpdateDTO.getOldPhone() == null || !userUpdateDTO.getOldPhone().equals(user.getUserPhone())) {
                throw new IllegalArgumentException("原手机号验证失败，无法修改手机号");
            }
            // 检查新手机号是否已存在
            if (userRepository.findAll().stream().anyMatch(u -> u.getUserPhone() != null && u.getUserPhone().equals(userUpdateDTO.getUserPhone()))) {
                throw new IllegalArgumentException("手机号: " + userUpdateDTO.getUserPhone() + " 已被占用");
            }
            user.setUserPhone(userUpdateDTO.getUserPhone());
        }

        // 3. 修改用户名 (昵称)
        if (userUpdateDTO.getUserName() != null && !userUpdateDTO.getUserName().equals(userName)) {
            if (userRepository.findByUserName(userUpdateDTO.getUserName()) != null) {
                throw new IllegalArgumentException("用户名: " + userUpdateDTO.getUserName() + " 已存在");
            }
            user.setUserName(userUpdateDTO.getUserName());
        }

        // 4. 其他字段更新 (若未输入则维持原值)
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
        if (userUpdateDTO.getUserStatus() != null) {
            user.setUserStatus(userUpdateDTO.getUserStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean updateUserStatusByAdmin(String userName, String status) {
        User_General user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userName);
        }
        if (!"正常".equals(status) && !"异常".equals(status)) {
            throw new IllegalArgumentException("非法的状态值，仅支持 '正常' 或 '异常'");
        }
        user.setUserStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean deleteUser(String userName) {
        User_General user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userName);
        }
        userRepository.delete(user);
        return true;
    }

    /**
     * 获取所有用户信息
     * @return 包含所有用户的列表
     */
    @Override
    public List<User_General> getAllUsers() {
        // 使用 JpaRepository 的 findAll 方法获取所有用户
        return userRepository.findAll();
    }
}