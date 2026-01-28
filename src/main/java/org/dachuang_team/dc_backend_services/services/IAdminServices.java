package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Dto.AdminDTO;
import java.util.List;

/**
 * 管理员服务接口
 * 定义管理员相关的业务逻辑方法
 */
public interface IAdminServices {
    /**
     * 注册管理员
     * @param adminDTO 包含注册信息的 DTO 对象
     */
    void registerAdmin(AdminDTO adminDTO);

    /**
     * 验证管理员登录
     * @param adminName 用户名
     * @param rawPassword 原始密码
     * @return 验证结果
     */
    boolean authenticateAdmin(String adminName, String rawPassword);

    /**
     * 根据用户名获取管理员信息
     * @param adminName 用户名
     * @return 管理员对象
     */
    AdminDTO getAdminByAdminName(String adminName);

    /**
     * 获取所有管理员信息
     * @return 管理员信息列表
     */
    List<AdminDTO> getAllAdmins();

    /**
     * 删除管理员
     * @param adminName 管理员用户名
     * @return 是否删除成功
     */
    boolean deleteAdmin(String adminName);
}
