package org.dachuang_team.dc_backend_services.services;
import org.dachuang_team.dc_backend_services.pojo.Dto.UserDTO;
import org.dachuang_team.dc_backend_services.pojo.User_General;

public interface IUserServices {
    void registerUser(UserDTO user);
    boolean authenticateUser(String userName, String rawPassword);
    User_General getUserByUserName(String userName);
}
