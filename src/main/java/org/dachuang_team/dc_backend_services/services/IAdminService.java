package org.dachuang_team.dc_backend_services.services;

import org.dachuang_team.dc_backend_services.domain.DTO.AdminDTO;

import java.util.List;

public interface IAdminService {

    void registerAdmin(AdminDTO adminDTO);

    String authenticateAdmin(String adminName, String rawPassword);

    AdminDTO getAdminByAdminName(String adminName);

    List<AdminDTO> getAllAdmins();

    boolean deleteAdmin(String targetAdminName, String currentAdminName, String currentAdminPassword);

    void updateAdminStatus(Long adminId, Integer status);

    void resetAdminPassword(Long adminId, String newPassword);
}
