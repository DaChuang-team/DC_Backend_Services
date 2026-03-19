package org.dachuang_team.dc_backend_services.services;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserUpdateDTO;
import org.dachuang_team.dc_backend_services.pojo.UserPO.UserGeneral;

import java.util.List;

public interface IUserService {
    void registerUser(UserDTO user);
    String authenticateUser(String userName, String rawPassword);
    UserGeneral getUserByUserName(String userName);
    boolean updateInfo(Long userId, UserUpdateDTO dto);
    /**
     * 管理员修改用户状态
     * @param userName 用户名
     * @param status 新状态
     * @return 是否修改成功
     */
    boolean updateUserStatusByAdmin(String userName, String status);
    /**
     * 删除用户
     * @param userName 用户名
     * @return 是否删除成功
     */
    boolean deleteUser(String userName);
    /**
     * 获取所有用户信息
     * @return 用户列表
     */
    List<UserGeneral> getAllUsers();
    /**
     * 根据用户ID获取用户实体
     * @param userId 用户唯一标识
     * @return User_General 实体
     */
    UserGeneral getUserById(Long userId);
    /**
     * 根据用户ID进行签到
     * @param userId 用户唯一标识
     * @return 是否签到成功
     */
    boolean checkIn(Long userId);

    /**
     * 扣除用户积分
     * @param userId 用户唯一标识
     * @param pointsToDeduct 要扣除的积分数量
     * @param reason 扣除积分的原因
     */
    void deductPoints(Long userId, int pointsToDeduct, String reason);

    /**
     * 用户登出
     * @param userId 用户唯一标识
     * @return 是否登出成功
     */
    boolean logout(long userId);
}
