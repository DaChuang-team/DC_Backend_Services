package org.dachuang_team.dc_backend_services.services;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import org.dachuang_team.dc_backend_services.pojo.Dto.userUpdateDTO;
import org.dachuang_team.dc_backend_services.pojo.User_General;

import java.util.List;

public interface IUserServices {
    void registerUser(UserDTO user);
    boolean authenticateUser(String userName, String rawPassword);
    User_General getUserByUserName(String userName);
    boolean updateInfo(String userName, userUpdateDTO userUpdateDTO);
    /**
     * 获取所有用户信息
     * @return 用户列表
     */
    List<User_General> getAllUsers();
}
