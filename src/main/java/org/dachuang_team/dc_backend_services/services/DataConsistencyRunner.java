package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.PO.Admin;
import org.dachuang_team.dc_backend_services.domain.PO.UserPO.UserGeneral;
import org.dachuang_team.dc_backend_services.repository.AdminRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataConsistencyRunner implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<UserGeneral> users = userRepository.findAll();
        for (UserGeneral u : users) {
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
