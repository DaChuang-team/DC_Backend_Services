package org.dachuang_team.dc_backend_services.repository;

import org.dachuang_team.dc_backend_services.pojo.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 管理员数据访问接口
 * 继承 JpaRepository 后，Spring 会自动实现基本的增删改查功能
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    /**
     * 根据用户名查找管理员
     * @param adminName 用户名
     * @return 管理员对象
     */
    Admin findByAdminName(String adminName);
}
