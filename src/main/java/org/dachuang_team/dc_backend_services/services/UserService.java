package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.config.RedisConfig;
import org.dachuang_team.dc_backend_services.enumeration.PointsChangeReason;
import org.dachuang_team.dc_backend_services.pojo.UserPO.UserCheckIn;
import org.dachuang_team.dc_backend_services.pojo.UserPO.UserGeneral;
import org.dachuang_team.dc_backend_services.repository.UserCheckInRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserUpdateDTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PointsRecordService pointsRecordService;

    @Autowired
    private UserCheckInRepository checkInRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    // 注册用户
    @Override
    public void registerUser(UserDTO user) {
        if (userRepository.findByUserName(user.getUserName()) != null) {
            throw new IllegalArgumentException("用户名: " + user.getUserName() + " 已存在");
        }
        LocalDateTime now = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        UserGeneral newUser = new UserGeneral();
        BeanUtils.copyProperties(user, newUser, "userPassword", "userGender"); // 复制除 userPassword 和 userGender 以外的属性
        newUser.setUserPassword(encodedPassword);
        newUser.setUserPermissions(0);
        newUser.setCreateTime(now);
        newUser.setUserStatus("正常");
        newUser.setPoints(0);
        //处理userGender：若DTO中为null，则设为'U'，否则设为JSON传入值
        newUser.setUserGender(user.getUserGender() != null ? user.getUserGender() : 'U');
        userRepository.save(newUser);
    }

    // 用户登录验证
    @Override
    @Transactional
    public String authenticateUser(String userName, String rawPassword) {
        try {
            logger.info("开始验证用户登录，用户名: {}", userName);

            UserGeneral user = userRepository.findByUserName(userName);

            // 基础校验
            if (user == null || !passwordEncoder.matches(rawPassword, user.getUserPassword())) {
                logger.warn("用户名或密码错误: {}", userName);
                throw new IllegalArgumentException("用户名或密码错误");
            }

            // 状态校验
            if ("异常".equals(user.getUserStatus())) {
                logger.warn("用户状态异常，禁止登录: {}", userName);
                throw new IllegalArgumentException("该用户状态异常，禁止登录");
            }

            // 更新最后登录时间
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            userRepository.save(user);
            logger.info("用户最后登录时间已更新: {}", now);

            // 生成并存储Token
            String token = authService.generateToken(user.getUserId(), "USER");
            logger.info("Token 生成成功: {}", token);

            return token;
        } catch (IllegalArgumentException e) {
            logger.error("登录失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("服务器错误: {}", e.getMessage(), e);
            throw new RuntimeException("登录时发生服务器错误");
        }
    }

    @Override
    public UserGeneral getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    @Transactional
    public boolean updateInfo(Long userId, UserUpdateDTO dto) {
        // 1. 直接根据 ID 找用户
        UserGeneral user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 2. 敏感信息修改需要额外验证
        boolean isChangingPassword = (dto.getUserPassword() != null && !dto.getUserPassword().isEmpty());
        boolean isChangingPhone = (dto.getUserPhone() != null && !dto.getUserPhone().equals(user.getUserPhone()));

        if (isChangingPassword) {
            // 修改密码需要验证旧密码
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), user.getUserPassword())) {
                throw new IllegalArgumentException("修改密码需提供正确的旧密码");
            }
            user.setUserPassword(passwordEncoder.encode(dto.getUserPassword()));
        }

        if (isChangingPhone) {
            // 修改手机号需要验证旧密码和旧手机号
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), user.getUserPassword())) {
                throw new IllegalArgumentException("修改手机号需提供正确的旧密码");
            }
            if (dto.getOldPhone() == null || !user.getUserPhone().equals(dto.getOldPhone())) {
                throw new IllegalArgumentException("修改手机号需提供正确的旧手机号");
            }
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
    private void updateNormalFields(UserGeneral user, UserUpdateDTO dto) {
        if (dto.getUserPreference() != null) user.setUserPreference(dto.getUserPreference());
        if (dto.getUserGender() != null) user.setUserGender(dto.getUserGender());
        if (dto.getUserAvatarURL() != null) user.setUserAvatarURL(dto.getUserAvatarURL());
        if (dto.getUserBirthday() != null) user.setUserBirthday(dto.getUserBirthday());
    }


    // 用户签到
    @Override
    @Transactional
    public boolean checkIn(Long userId) {
        // 获取用户信息
        UserGeneral user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if ("异常".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("该用户状态异常，禁止签到");
        }

        // 时间范围判定：今日 00:00:00 到 23:59:59
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

        // 校验今日是否已签到
        if (checkInRepository.existsByUserIdAndCheckInTimeBetween(userId, todayStart, todayEnd)) {
            throw new IllegalArgumentException("今日已签到，请明天再来");
        }

        // 在签到表插入一条签到记录
        UserCheckIn checkIn = new UserCheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInTime(LocalDateTime.now());
        checkIn.setPointsEarned(10);
        checkInRepository.save(checkIn);

        // 在用户表更新积分余额
        user = userRepository.findById(userId).get();
        int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + 10;
        user.setPoints(newPoints);

        userRepository.save(user); // 更新用户表

        pointsRecordService.addPointsRecord(userId, 10, "CHECK_IN");

        return true;
    }

    // 扣除用户积分
    @Override
    public void deductPoints(Long userId, int pointsToDeduct, String reason) {
        // 查询用户
        UserGeneral user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 校验用户状态
        if ("异常".equals(user.getUserStatus())) {
            throw new IllegalArgumentException("该用户状态异常，AI功能受限");
        }

        if(!PointsChangeReason.isValidReason(reason)) {
            throw new IllegalArgumentException("无效的积分变动原因: " + reason);
        }

        // 获取当前积分并校验
        int currentPoints = user.getPoints() == null ? 0 : user.getPoints();
        if (currentPoints < pointsToDeduct) {
            throw new IllegalArgumentException("积分不足，无法扣除");
        }

        // 扣除积分并保存
        user.setPoints(currentPoints - pointsToDeduct);
        pointsRecordService.addPointsRecord(userId, (pointsToDeduct)*(-1), reason);
        userRepository.save(user);
    }

    @Override
    public boolean logout(long userId) {
        authService.invalidateToken(userId, "USER");
        return true;
    }


    @Override
    public boolean updateUserStatusByAdmin(String userName, String status) {
        UserGeneral user = userRepository.findByUserName(userName);
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
        UserGeneral user = userRepository.findByUserName(userName);
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
    public List<UserGeneral> getAllUsers() {
        // 使用 JpaRepository 的 findAll 方法获取所有用户
        return userRepository.findAll();
    }

    @Override
    public UserGeneral getUserById(Long userId) {
        // 使用 findById(id)，如果找不到则抛出异常，这能保证后续业务拿到的一定是有效对象
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("未找到 ID 为 " + userId + " 的用户"));
    }
}
