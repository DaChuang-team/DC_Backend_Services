package org.dachuang_team.dc_backend_services.controller;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.FileUploadResponseDTO;
import org.dachuang_team.dc_backend_services.pojo.ProductImageRecord;
import org.dachuang_team.dc_backend_services.pojo.SysImage;
import org.dachuang_team.dc_backend_services.repository.ProductImageRecordRepository;
import org.dachuang_team.dc_backend_services.repository.SysImageRepository;
import org.dachuang_team.dc_backend_services.services.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 图片控制器
 * 处理与图片相关的HTTP请求
 */
@RestController
@RequestMapping("/api")
public class ImageController {

    @Autowired
    private IStorageService storageService;

    @Autowired
    private ProductImageRecordRepository productImageRecordRepository;
    @Autowired
    private SysImageRepository sysImageRepository;

    @PutMapping("/image/upload")
    @Transactional
    public Result<FileUploadResponseDTO> uploadImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);

        // 调用存储服务保存物理文件
        IStorageService.StorageResult result = storageService.upload(file);

        // 创建并保存图片记录信息
        ProductImageRecord record = new ProductImageRecord();
        record.setUrl(result.getUrl());
        record.setPhysicalPath(result.getPhysicalPath());
        record.setCreatedAt(LocalDateTime.now());
        record.setLinked(false); // 初始为未绑定

        productImageRecordRepository.save(record);

        // 构造并返回要求的 DTO
        FileUploadResponseDTO response = new FileUploadResponseDTO();
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    //用户中途取消上传商品时，清理已上传但未绑定的图片记录和物理文件，不可用于修改商品时删除已绑定的图片
    @DeleteMapping("/image/purge")
    @Transactional
    public Result<String> deleteImg(@RequestParam String url) {
        // 先删数据库记录，再删物理文件
        Optional<ProductImageRecord> record = productImageRecordRepository.findByUrl(url);
        if (record.isEmpty()) return Result.error(404, "图片记录未找到");
        ProductImageRecord productImageRecord = record.get();
        if (productImageRecord.getLinked()) return Result.error(400, "图片已绑定到商品，无法删除");
        storageService.delete(url);
        productImageRecordRepository.delete(productImageRecord);
        return Result.success("图片已清理");
    }

    // 管理员接口，上传系统图片资源用于首页或其他区域展示
    @PutMapping("/sysImg/upload")
    public Result<FileUploadResponseDTO> uploadSysImg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("purpose") String purpose) { //purpose参数定义见sysImage实体类注释
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);
        if (!"ROLE_ADMIN".equals(getCurrentUserRole())) return Result.error(403, "权限不足");
        if(purpose == null || purpose.isEmpty()) return Result.error(400, "用途参数不能为空");
        else if (!purpose.equals("MAIN_PAGE_BANNER") &&
                 !purpose.equals("PRODUCT_PAGE_BANNER") &&
                 !purpose.equals("USER_SYS_AVATAR")) return Result.error(400, "用途参数值无效");

        IStorageService.StorageResult result = storageService.upload(file);

        SysImage record = new SysImage();
        record.setImageUrl(result.getUrl());
        record.setPurpose(purpose);
        record.setImageName(result.getFileName());
        sysImageRepository.save(record);

        FileUploadResponseDTO response = new FileUploadResponseDTO();
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    //用于获取系统图片资源URL，前端根据用途参数调用此接口获取对应图片URL进行展示
    @GetMapping("/sysImg/get")
    public Result<List<Map<String, Object>>> getSysImg(@RequestParam("purpose") String purpose) {
        if (purpose == null || purpose.isEmpty()) return Result.error(400, "用途参数不能为空");

        List<SysImage> records = sysImageRepository.findByPurpose(purpose);
        if (records.isEmpty()) return Result.error(404, "所属用途的图片未找到");

        List<Map<String, Object>> result = records.stream().map(record -> Map.<String, Object>of(
                "imageUrl", record.getImageUrl(),
                "imageName", record.getImageName()
        )).toList();

        return Result.success("查询成功", result);
    }

    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
    }
}