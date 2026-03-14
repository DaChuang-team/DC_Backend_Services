package org.dachuang_team.dc_backend_services.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalStorageService implements IStorageService {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public StorageResult upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = null;
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            //抛出异常，文件名不能为空
            throw new RuntimeException("文件名不能为空");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        File targetDir = new File(uploadPath);
        if (!targetDir.exists()) targetDir.mkdirs();

        File dest = new File(targetDir, fileName);
        try {
            file.transferTo(dest);
            // 返回包含 URL 和 物理路径的结果
            return new StorageResult("/uploadedImg/" + fileName, dest.getAbsolutePath(), fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传至本地失败", e);
        }
    }

    @Override
    public void delete(String url) {
        // 根据 URL 解析出文件名，结合 uploadPath 找到物理文件
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        File file = new File(uploadPath, fileName);
        if (file.exists()) {
            file.delete();
        }
    }


}