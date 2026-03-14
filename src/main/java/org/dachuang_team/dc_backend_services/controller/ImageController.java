package org.dachuang_team.dc_backend_services.controller;

import jakarta.transaction.Transactional;
import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Dto.FileUploadResponse;
import org.dachuang_team.dc_backend_services.pojo.ImageRecord;
import org.dachuang_team.dc_backend_services.repository.ImageRecordRepository;
import org.dachuang_team.dc_backend_services.services.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private ImageRecordRepository imageRecordRepository;

    @PostMapping("/image/upload")
    @Transactional
    public Result<FileUploadResponse> uploadImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);

        // 调用存储服务保存物理文件
        IStorageService.StorageResult result = storageService.upload(file);

        // 创建并保存图片记录信息
        ImageRecord record = new ImageRecord();
        record.setUrl(result.getUrl());
        record.setPhysicalPath(result.getPhysicalPath());
        record.setCreatedAt(LocalDateTime.now());
        record.setLinked(false); // 初始为未绑定

        imageRecordRepository.save(record);

        // 构造并返回要求的 DTO
        FileUploadResponse response = new FileUploadResponse();
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    //用户中途取消上传商品时，清理已上传但未绑定的图片记录和物理文件，不可用于修改商品时删除已绑定的图片
    @DeleteMapping("/image/purge")
    @Transactional
    public Result<String> deleteImg(@RequestParam String url) {
        // 先删数据库记录，再删物理文件
        Optional<ImageRecord> record = imageRecordRepository.findByUrl(url);
        if (record.isEmpty()) return Result.error(404, "图片记录未找到");
        ImageRecord imageRecord = record.get();
        if (imageRecord.getLinked()) return Result.error(400, "图片已绑定到商品，无法删除");
        storageService.delete(url);
        imageRecordRepository.delete(imageRecord);
        return Result.success("图片已清理");
    }

    private Long getCurrentUserId() {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUserId == null) {
            throw new SecurityException("未认证");
        }
        return currentUserId;
    }

    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
    }
}