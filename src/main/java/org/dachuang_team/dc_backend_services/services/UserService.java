package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.pojo.UserSession;
import org.dachuang_team.dc_backend_services.pojo.User_General;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.dachuang_team.dc_backend_services.repository.UserSessionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.userUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements IUserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    @Autowired
    private AuthService authService;

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
        BeanUtils.copyProperties(user, newUser, "userPassword", "userGender"); // 复制除 userPassword 和 userGender 以外的属性
        newUser.setUserPassword(encodedPassword);
        newUser.setUserPermissions(0);
        newUser.setCreateTime(now);
        newUser.setUserStatus("正常");
        //处理userGender：若DTO中为null，则设为'U'，否则设为JSON传入值
        newUser.setUserGender(user.getUserGender() != null ? user.getUserGender() : 'U');
        userRepository.save(newUser);
    }

    // 用户登录验证
    @Override
    @Transactional
    public String authenticateUser(String userName, String rawPassword) {
        User_General user = userRepository.findByUserName(userName);

        // 1. 基础校验
        if (user == null || !passwordEncoder.matches(rawPassword, user.getUserPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 2. 状态校验
        if ("异常".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("该用户状态异常，禁止登录");
        }

        // 3. 更新最后登录时间
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginAt(now);
        userRepository.save(user);

        // 4. 生成并存储 Token
        return authService.generateToken(user.getUserId());
    }

    @Override
    public User_General getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    @Transactional
    public boolean updateInfo(Long userId, userUpdateDTO dto) {
        // 1. 直接根据 ID 找用户，效率更高
        User_General user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 2. 敏感操作校验或手机号
        boolean isChangingPassword = (dto.getUserPassword() != null && !dto.getUserPassword().isEmpty());
        boolean isChangingPhone = (dto.getUserPhone() != null && !dto.getUserPhone().equals(user.getUserPhone()));

        if (isChangingPassword || isChangingPhone) {
            // 验证旧密码是否匹配
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), user.getUserPassword())) {
                throw new IllegalArgumentException("修改敏感信息需提供正确的旧密码");
            }
        }

        // 3. 处理密码更新
        if (isChangingPassword) {
            user.setUserPassword(passwordEncoder.encode(dto.getUserPassword()));
        }

        // 4. 处理手机号更新（需验证唯一性）
        if (isChangingPhone) {
            // 检查新手机号是否冲突
            if (userRepository.existsByUserPhone(dto.getUserPhone())) {
                throw new IllegalArgumentException("手机号已存在");
            }
            user.setUserPhone(dto.getUserPhone());
        }

        // 5. 处理用户名/昵称更新
        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())) {
            if (userRepository.existsByUserName(dto.getUserName())) {
                throw new IllegalArgumentException("该用户名已被占用");
            }
            user.setUserName(dto.getUserName());
        }

        // 6. 其他普通字段
        updateNormalFields(user, dto);

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    // 抽取非敏感字段更新逻辑
    private void updateNormalFields(User_General user, userUpdateDTO dto) {
        if (dto.getUserPreference() != null) user.setUserPreference(dto.getUserPreference());
        if (dto.getUserGender() != null) user.setUserGender(dto.getUserGender());
        if (dto.getUserAvatarURL() != null) user.setUserAvatarURL(dto.getUserAvatarURL());
        if (dto.getUserBirthday() != null) user.setUserBirthday(dto.getUserBirthday());
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

    @Override
    public User_General getUserById(Long userId) {
        // 使用 findById(id)，如果找不到则抛出异常，这能保证后续业务拿到的一定是有效对象
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("未找到 ID 为 " + userId + " 的用户"));
    }
}
