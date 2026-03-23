package org.dachuang_team.dc_backend_services.services;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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
    public StorageResult uploadByFile(MultipartFile file) {
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
    public String uploadByByte(byte[] fileBytes, String fileName) {
        // 初始化OSS客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            // 上传文件到 OSS
            ossClient.putObject(bucketName, fileName, inputStream);

            // 拼接访问 URL
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileName;
            return fileUrl;
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
        if (url == null || url.isEmpty()) return;

        String key = "";
        try {
            // 使用URL类提取路径例如：/products/XXX/XXX.jpg
            java.net.URL parsedUrl = new java.net.URL(url);
            key = parsedUrl.getPath();
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("解析待删除的 URL 失败: " + url, e);
        }
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 使用完整的 Key 进行删除
            ossClient.deleteObject(bucketName, key);
        } catch (Exception e) {
            throw new RuntimeException("OSS 文件删除失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public byte[] downloadByUrl(String url) {
        String key = "";
        try {
            // 解析URL路径
            java.net.URL parsedUrl = new java.net.URL(url);
            key = parsedUrl.getPath(); // 这里获取到的是 "/products/XXX/XXX.jpg"

            if (key.startsWith("/")) {
                key = key.substring(1);
            }
        } catch (Exception e) {
            throw new RuntimeException("解析图片 URL 失败: " + url, e);
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 使用完整的 Key：products/XXX/XXX.jpg
            OSSObject ossObject = ossClient.getObject(bucketName, key);
            InputStream inputStream = ossObject.getObjectContent();
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("从 OSS 下载图片字节流失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}