package org.dachuang_team.dc_backend_services.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    // 上传并返回存储结果（包含物理路径和访问URL）
    StorageResult upload(MultipartFile file);

    // 根据URL删除物理文件
    void delete(String url);

    // 内部辅助类
    @Data
    @AllArgsConstructor
    class StorageResult {
        private String url;          // 相对访问路径
        private String physicalPath; // 绝对物理路径
        private String fileName;     // 存储的文件名
    }
}
