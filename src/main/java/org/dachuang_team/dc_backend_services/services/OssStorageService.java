package org.dachuang_team.dc_backend_services.services;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "oss")
public class OssStorageService implements IStorageService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Override
    public StorageResult upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix;
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            throw new RuntimeException("文件名不能为空且必须包含后缀");
        }

        // 生成云端唯一文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 初始化OSS客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到 OSS
            ossClient.putObject(bucketName, fileName, inputStream);

            // 拼接访问 URL
            // 格式为: https://bucketName.endpoint/fileName
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;

            // 方便后续调用 delete 方法时直接提取
            return new StorageResult(fileUrl, fileName, fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传至 OSS 失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void delete(String url) {
        // 从 URL 中解析出 OSS 的 ObjectName (即文件名)
        // 假设 URL 格式为 https://bucket.endpoint/filename.jpg
        String fileName = url.substring(url.lastIndexOf("/") + 1);

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 执行 OSS 删除操作
            ossClient.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new RuntimeException("OSS 文件删除失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}