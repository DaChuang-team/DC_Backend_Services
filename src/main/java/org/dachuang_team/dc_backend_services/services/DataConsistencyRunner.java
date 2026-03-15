package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.pojo.Admin;
import org.dachuang_team.dc_backend_services.pojo.userGeneral;
import org.dachuang_team.dc_backend_services.repository.adminRepository;
import org.dachuang_team.dc_backend_services.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataConsistencyRunner implements ApplicationRunner {

    @Autowired
    private userRepository userRepository;
    @Autowired
    private adminRepository adminRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<userGeneral> users = userRepository.findAll();
        for (userGeneral u : users) {
            String status = u.getUserStatus();
            if (status == null || status.isBlank()) {
                u.setUserStatus("正常");
            } else if (!"正常".equals(status) && !"异常".equals(status)) {
                u.setUserStatus("正常");
            }
            char gender = u.getUserGender();
            if (gender != 'M' && gender != 'F' && gender != 'U') {
                u.setUserGender('U');
            }
        }
        userRepository.saveAll(users);

        List<Admin> admins = adminRepository.findAll();
        for (Admin a : admins) {
            String role = a.getAdminRole();
            if (role == null || role.isBlank()) {
                a.setAdminRole("ADMIN");
            } else if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
                a.setAdminRole("ADMIN");
            }
        }
        adminRepository.saveAll(admins);
    }
}
