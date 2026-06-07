package org.dachuang_team.dc_backend_services.services;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.config.RedisConfig;
import org.dachuang_team.dc_backend_services.domain.PO.Admin;
import org.dachuang_team.dc_backend_services.domain.DTO.AdminDTO;
import org.dachuang_team.dc_backend_services.domain.VO.DailyOrderStatsVO;
import org.dachuang_team.dc_backend_services.domain.VO.MonthlyStatsVO;
import org.dachuang_team.dc_backend_services.domain.VO.OrderStatsVO;
import org.dachuang_team.dc_backend_services.domain.VO.ProductCategoryStatsVO;
import org.dachuang_team.dc_backend_services.repository.AdminRepository;
import org.dachuang_team.dc_backend_services.repository.OrderRepository;
import org.dachuang_team.dc_backend_services.repository.ProductRepository;
import org.dachuang_team.dc_backend_services.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService implements IAdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Autowired
    AuthService authService;

    @Override
    public void registerAdmin(AdminDTO adminDTO) {
        if (adminDTO.getAdminRole() == null || (!"ADMIN".equals(adminDTO.getAdminRole()) && !"SUPER_ADMIN".equals(adminDTO.getAdminRole()))) {
            throw new IllegalArgumentException("角色无效，必须为 ADMIN 或 SUPER_ADMIN");
        }
        if (adminRepository.findByAdminName(adminDTO.getAdminName()) != null) {
            throw new IllegalArgumentException("管理员名称: " + adminDTO.getAdminName() + " 已存在");
        }
        Admin newAdmin = new Admin();
        newAdmin.setAdminName(adminDTO.getAdminName());
        String encodedPassword = passwordEncoder.encode(adminDTO.getAdminPassword());
        newAdmin.setAdminPassword(encodedPassword);
        newAdmin.setAdminRole(adminDTO.getAdminRole());
        adminRepository.save(newAdmin);
    }

    @Override
    @Transactional
    public String authenticateAdmin(String adminName, String rawPassword) {
        LocalDateTime now = LocalDateTime.now();
        Admin admin = adminRepository.findByAdminName(adminName);

        if (admin == null || !passwordEncoder.matches(rawPassword, admin.getAdminPassword())) {
            logger.warn("用户名或密码错误: {}", adminName);
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (admin.getStatus() != null && admin.getStatus() == 1) {
            logger.warn("账号已被禁用: {}", adminName);
            throw new IllegalArgumentException("该账号已被禁用，请联系超级管理员");
        }

        admin.setLastLogin(now);
        adminRepository.save(admin);

        String token = authService.generateToken(admin.getAdminId(), admin.getAdminRole());
        logger.info("管理员登录成功: adminName={}", adminName);
        return token;
    }

    @Override
    public AdminDTO getAdminByAdminName(String adminName) {
        Admin admin = adminRepository.findByAdminName(adminName);
        if (admin == null) {
            return null;
        }
        AdminDTO dto = new AdminDTO();
        dto.setAdminName(admin.getAdminName());
        dto.setAdminRole(admin.getAdminRole());
        return dto;
    }

    @Override
    public List<AdminDTO> getAllAdmins() {
        List<Admin> adminList = adminRepository.findAll();
        return adminList.stream().map(admin -> {
            AdminDTO dto = new AdminDTO();
            dto.setAdminName(admin.getAdminName());
            dto.setAdminRole(admin.getAdminRole());
            dto.setAdminId(admin.getAdminId());
            dto.setLastLogin(admin.getLastLogin());
            dto.setStatus(admin.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteAdmin(String targetAdminName, String currentAdminName, String currentAdminPassword) {
        Admin currentAdmin = adminRepository.findByAdminName(currentAdminName);
        if (currentAdmin == null || !passwordEncoder.matches(currentAdminPassword, currentAdmin.getAdminPassword())) {
            throw new IllegalArgumentException("当前管理员身份验证失败");
        }
        if (!"SUPER_ADMIN".equals(currentAdmin.getAdminRole())) {
            throw new IllegalArgumentException("只有超级管理员可以删除管理员");
        }
        Admin admin = adminRepository.findByAdminName(targetAdminName);
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在: " + targetAdminName);
        }
        if (targetAdminName.equals(currentAdminName)) {
            throw new IllegalArgumentException("超级管理员不能删除自己");
        }
        adminRepository.delete(admin);
        return true;
    }

    @Override
    public void updateAdminStatus(Long adminId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态值无效，必须为 0（正常）或 1（禁用）");
        }
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("管理员不存在: " + adminId));
        if ("SUPER_ADMIN".equals(admin.getAdminRole())) {
            throw new IllegalArgumentException("不能禁用超级管理员账号");
        }
        admin.setStatus(status);
        adminRepository.save(admin);
        logger.info("管理员状态已更新: adminId={}, status={}", adminId, status);
    }

    @Override
    public void resetAdminPassword(Long adminId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("密码长度至少为6位");
        }
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("管理员不存在: " + adminId));
        String encodedPassword = passwordEncoder.encode(newPassword);
        admin.setAdminPassword(encodedPassword);
        adminRepository.save(admin);
        logger.info("管理员密码已重置: adminId={}", adminId);
    }

    public Map<String, OrderStatsVO> getOrderStatsForPeriods() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime ninetyDaysAgo = now.minusDays(90);

        long count7d = orderRepository.countByCreatedAtBetween(sevenDaysAgo, now);
        BigDecimal sales7d = orderRepository.sumTotalAmountByCreatedAtBetween(sevenDaysAgo, now);

        long count30d = orderRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        BigDecimal sales30d = orderRepository.sumTotalAmountByCreatedAtBetween(thirtyDaysAgo, now);

        long count90d = orderRepository.countByCreatedAtBetween(ninetyDaysAgo, now);
        BigDecimal sales90d = orderRepository.sumTotalAmountByCreatedAtBetween(ninetyDaysAgo, now);

        Map<String, OrderStatsVO> result = new HashMap<>();
        result.put("last7Days", new OrderStatsVO(count7d, sales7d));
        result.put("last30Days", new OrderStatsVO(count30d, sales30d));
        result.put("last90Days", new OrderStatsVO(count90d, sales90d));

        return result;
    }

    public List<ProductCategoryStatsVO> getProductCategoryStats() {
        List<Object[]> results = productRepository.countByCategory();
        List<ProductCategoryStatsVO> statsList = new ArrayList<>();
        for (Object[] row : results) {
            Integer category = (Integer) row[0];
            Long count = (Long) row[1];
            statsList.add(new ProductCategoryStatsVO(category, count));
        }
        return statsList;
    }

    public List<MonthlyStatsVO> getSixMonthStats() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6).truncatedTo(java.time.temporal.ChronoUnit.DAYS);

        List<Object[]> userResults = userRepository.findMonthlyUserStats(sixMonthsAgo);
        List<Object[]> orderResults = orderRepository.findMonthlyOrderStats(sixMonthsAgo);
        List<Object[]> productResults = productRepository.findMonthlyProductStats(sixMonthsAgo);

        Map<String, Long> userMap = new HashMap<>();
        for (Object[] row : userResults) {
            userMap.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        Map<String, Long> orderMap = new HashMap<>();
        Map<String, BigDecimal> salesMap = new HashMap<>();
        for (Object[] row : orderResults) {
            String month = row[0].toString();
            orderMap.put(month, ((Number) row[1]).longValue());
            salesMap.put(month, (BigDecimal) row[2]);
        }

        Map<String, Long> productMap = new HashMap<>();
        for (Object[] row : productResults) {
            productMap.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        List<MonthlyStatsVO> monthlyStats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            String month = java.time.YearMonth.now().minusMonths(i).format(formatter);
            Long users = userMap.getOrDefault(month, 0L);
            Long orders = orderMap.getOrDefault(month, 0L);
            BigDecimal sales = salesMap.getOrDefault(month, BigDecimal.ZERO);
            Long products = productMap.getOrDefault(month, 0L);
            monthlyStats.add(new MonthlyStatsVO(month, users, orders, sales, products));
        }

        return monthlyStats;
    }

    public List<DailyOrderStatsVO> getDailyOrderStatsForLast7Days() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        List<Object[]> results = orderRepository.findDailyOrderStats(sevenDaysAgo);

        Map<String, DailyOrderStatsVO> statsMap = new HashMap<>();
        for (Object[] row : results) {
            String date = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            BigDecimal total = (BigDecimal) row[2];
            statsMap.put(date, new DailyOrderStatsVO(date, count, total));
        }

        List<DailyOrderStatsVO> dailyStats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = java.time.LocalDate.now().minusDays(i).format(formatter);
            if (statsMap.containsKey(date)) {
                dailyStats.add(statsMap.get(date));
            } else {
                dailyStats.add(new DailyOrderStatsVO(date, 0L, BigDecimal.ZERO));
            }
        }

        return dailyStats;
    }
}
