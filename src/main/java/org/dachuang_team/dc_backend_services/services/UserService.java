package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.ImageProcessUtils;
import org.dachuang_team.dc_backend_services.config.RedisConfig;
import org.dachuang_team.dc_backend_services.enumeration.PointsChangeReason;
import org.dachuang_team.dc_backend_services.domain.DTO.UserAddressDTO;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.UserAvatar;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserAddress;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserCheckIn;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserGeneral;
import org.dachuang_team.dc_backend_services.enumeration.SmsScene;
import org.dachuang_team.dc_backend_services.repository.UserAddressRepository;
import org.dachuang_team.dc_backend_services.repository.UserAvatarRecordRepository;
import org.dachuang_team.dc_backend_services.repository.UserCheckInRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.dachuang_team.dc_backend_services.domain.DTO.UserDTO;
import org.dachuang_team.dc_backend_services.domain.DTO.UserUpdateDTO;

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
    UserAvatarRecordRepository avatarRecordRepository;

    @Autowired
    private UserCheckInRepository checkInRepository;

    @Autowired
    private UserAddressRepository addressRepository;

    @Autowired
    private ImageProcessUtils imageProcessUtils;

    @Autowired
    private SmsCodeService smsCodeService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    // 注册用户
    @Override
    public void registerUser(UserDTO user, String code) {

        boolean isCodeValid = smsCodeService.verifyCode(user.getUserPhone(), code, SmsScene.REGISTER);
        if (!isCodeValid) {
            throw new IllegalArgumentException("验证码错误");
        }
        if(user.getUserName() == null || user.getUserName().trim().isEmpty()) {
            user.setUserName("用户" + user.getUserPhone());
        }
        if(user.getUserPassword() == null || user.getUserPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if(user.getUserPhone() == null || user.getUserPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if(code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (userRepository.findByUserName(user.getUserName()) != null) {
            throw new IllegalArgumentException("用户名: " + user.getUserName() + " 已存在");
        }
        if (userRepository.findByUserPhone(user.getUserPhone()) != null) {
            throw new IllegalArgumentException("手机号: " + user.getUserPhone() + " 已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        String encodedPassword = passwordEncoder.encode(user.getUserPassword());
        UserGeneral newUser = new UserGeneral();
        BeanUtils.copyProperties(user, newUser, "userPassword", "userGender"); // 复制除 userPassword 和 userGender 以外的属性
        newUser.setUserPassword(encodedPassword);
        newUser.setCreateTime(now);
        newUser.setUserStatus("正常");
        newUser.setPoints(0);
        newUser.setUserGender(user.getUserGender() != null ? user.getUserGender() : 'U');
        userRepository.save(newUser);
    }

    @Override
    public String registerChecker(String userPhone, String userName) {
        if (userPhone != null && !userPhone.isBlank() && userName != null && !userName.isBlank()) {
            return "一次最多检验一个字段，请分开验证";
        }
        if (userPhone != null && !userPhone.isBlank()) {
            return userRepository.existsByUserPhone(userPhone.trim()) ? "手机号已被注册" : "OK";
        }
        if (userName != null && !userName.isBlank()) {
            return userRepository.existsByUserName(userName.trim()) ? "用户名已存在" : "OK";
        }
        return "OK";
    }


    @Override
    public void sendVerificationCode(String userPhone, SmsScene scene){
        // 如果是注册场景，批准未注册的是手机号发送验证码，否则必须要注册
        if (scene == SmsScene.REGISTER) {
            if (userRepository.existsByUserPhone(userPhone)) {
                throw new IllegalArgumentException("手机号已被注册");
            }
        } else {
            if (!userRepository.existsByUserPhone(userPhone)) {
                throw new IllegalArgumentException("手机号未注册");
            }
        }
        smsCodeService.sendCode(userPhone, scene);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public String authenticateUserBySms(String userPhone, String code) {
        try {
            logger.info("开始验证用户登录（短信方式），手机号: {}", userPhone);

            UserGeneral user = userRepository.findByUserPhone(userPhone);
            if (user == null) {
                logger.warn("用户登录失败，手机号: {}，原因: 用户不存在", userPhone);
                throw new IllegalArgumentException("用户手机号错误");
            }

            if ("异常".equals(user.getUserStatus())) {
                logger.warn("用户状态异常，禁止登录，手机号: {}", userPhone);
                throw new IllegalArgumentException("该用户状态异常，禁止登录");
            }

            // 验证验证码
            boolean isCodeValid = smsCodeService.verifyCode(userPhone, code, SmsScene.LOGIN);
            if (!isCodeValid) {
                logger.warn("用户登录失败，手机号: {}，原因: 验证码错误", userPhone);
                throw new IllegalArgumentException("验证码错误");
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

    // 用户登录验证
    @Override
    @Transactional(rollbackOn = Exception.class)
    public String authenticateUserByPassword(String userPhone, String rawPassword) {
        try {
            logger.info("开始验证用户登录，手机号: {}", userPhone);

            UserGeneral user = userRepository.findByUserPhone(userPhone);

            // 基础校验
            if (user == null || !passwordEncoder.matches(rawPassword, user.getUserPassword())) {
                logger.warn("用户登录失败，手机号: {}，原因: 用户不存在或密码错误", userPhone);
                throw new IllegalArgumentException("用户手机号或密码错误");
            }

            // 状态校验
            if ("异常".equals(user.getUserStatus())) {
                logger.warn("用户状态异常，禁止登录，手机号: {}", userPhone);
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
    @Transactional
    public boolean updateInfo(Long userId, UserUpdateDTO dto) {
        // 直接根据 ID 找用户
        UserGeneral user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 敏感信息修改需要额外验证
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

        // 处理用户名/昵称更新
        if (dto.getUserName() != null && !dto.getUserName().equals(user.getUserName())) {
            if (userRepository.existsByUserName(dto.getUserName())) {
                throw new IllegalArgumentException("该用户名已被占用");
            }
            user.setUserName(dto.getUserName());
        }

        // 他普通字段
        updateNormalFields(user, dto);

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return true;
    }

    // 抽取非敏感字段更新逻辑
    private void updateNormalFields(UserGeneral user, UserUpdateDTO dto) {
        if (dto.getUserPreference() != null) user.setUserPreference(dto.getUserPreference());
        if (dto.getUserGender() != null) user.setUserGender(dto.getUserGender());
        if (dto.getUserAvatarURL() != null) {
            // 先解绑所有旧头像（如果有）
            List<UserAvatar> userOldAvatars = avatarRecordRepository.findByUserId(user.getUserId());
            if (userOldAvatars != null && !userOldAvatars.isEmpty()) {
                userOldAvatars.forEach(avatar -> avatar.setLinked(false));
            }

            // 再绑定新头像，并且校验这个头像URL确实存在，并且是当前用户上传的（即avatarRecord里有记录，并且记录的userId和当前用户一致）
            UserAvatar userAvatar = avatarRecordRepository.findByAvatarUrl(dto.getUserAvatarURL());
            if (userAvatar == null) {
                throw new IllegalArgumentException("当前头像不存在，请先上传头像");
            } else if (!userAvatar.getUserId().equals(user.getUserId())) {
                throw new IllegalArgumentException("没有权限访问当前头像资源");
            } else {
                String url = imageProcessUtils.userAvatarProcess(userAvatar, user.getUserId());
                user.setUserAvatarURL(url);
            }
        }
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
        try {
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
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean logout(long userId) {
        authService.invalidateToken(userId, "USER");
        return true;
    }

    @Override
    public List<UserAddress> getUserAddresses (Long userId){
        return addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
    }

    @Override
    @Transactional
    public void addUserAddress(Long userId, UserAddressDTO addressDTO) {
        int count = addressRepository.countByUserId(userId);
        if (count >= 10) {
            throw new IllegalArgumentException("地址数量已达上限");
        }

        // 如果是第一条，或者前端传了 true
        boolean shouldBeDefault = (count == 0) || (addressDTO.isDefault());

        if (shouldBeDefault) {
            addressRepository.resetDefaultByUserId(userId);
        }

        UserAddress newAddress = new UserAddress();
        BeanUtils.copyProperties(addressDTO, newAddress);

        //
        newAddress.setUserId(userId);
        newAddress.setDefault(shouldBeDefault);
        newAddress.setCreatedAt(LocalDateTime.now());
        newAddress.setUpdatedAt(LocalDateTime.now());

        // 备注字段处理
        if (newAddress.getRemarks() == null) {
            newAddress.setRemarks("");
        }

        addressRepository.save(newAddress);
    }

    @Override
    @Transactional
    public void updateUserAddress(Long userId, Long addressId, UserAddressDTO addressDTO) {
        // 校验现有是否存在
        UserAddress existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("目标更新地址不存在"));

        // 验证该地址是否属于当前用户
        if (!existingAddress.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该地址");
        }

        // 如果用户想将当前地址设为默认，且它原本不是默认
        if (addressDTO.isDefault() && !existingAddress.isDefault()) {
            // 将该用户下其他所有地址设为非默认
            addressRepository.resetDefaultByUserId(userId);
            // 把当前地址设为默认
            existingAddress.setDefault(true);
        }

        BeanUtils.copyProperties(addressDTO, existingAddress, "id", "userId", "createdAt", "updatedAt");
        existingAddress.setUpdatedAt(LocalDateTime.now());

        addressRepository.save(existingAddress);
    }

    @Override
    @Transactional
    public void deleteUserAddress(Long userId, Long addressId) {
        UserAddress existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));

        if (!existingAddress.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该地址");
        }

        addressRepository.delete(existingAddress);

    }

    @Override
    @Transactional
    public void setDefaultUserAddress(Long userId, Long addressId) {
        UserAddress addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));

        if (!addr.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该地址");
        }

        // 只有当前地址不是默认时才执行更新，避免浪费性能
        if (!addr.isDefault()) {
            addressRepository.resetDefaultByUserId(userId);
            addressRepository.updateDefaultStatus(addressId, true);
        }
    }

    @Override
    @Transactional
    public void unsetDefaultUserAddress(Long userId, Long addressId) {
        UserAddress addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址不存在"));

        if (!addr.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该地址");
        }
            addressRepository.updateDefaultStatus(addressId, false);
    }

    @Override
    @Transactional
    public UserAddress getDefaultUserAddress(Long userId) {
        // 查询所有默认地址
        List<UserAddress> defaultAddresses = addressRepository.findByUserIdAndIsDefaultTrue(userId);

        if (defaultAddresses == null || defaultAddresses.isEmpty()) {
            return null;
        }

        if (defaultAddresses.size() == 1) {
            // 只有一个默认地址，直接返回
            return defaultAddresses.get(0);
        }

        // 有多个默认地址，修正数据
        // 先全部取消默认
        addressRepository.resetDefaultByUserId(userId);

        // 找到最近更新的地址
        List<UserAddress> allAddresses = addressRepository.findByUserIdOrderByUpdatedAtDesc(userId);
        if (allAddresses.isEmpty()) {
            return null;
        }
        UserAddress latest = allAddresses.get(0);
        addressRepository.updateDefaultStatus(latest.getId(), true);

        // 返回修正后的默认地址
        return latest;
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
