package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.pojo.Image;
import org.dachuang_team.dc_backend_services.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图片控制器
 * 处理与图片相关的HTTP请求
 */
@RestController
@RequestMapping("/api")
public class ImageController {

    /**
     * 图片数据访问层
     */
    @Autowired
    private ImageRepository imageRepository;

    /**
     * 获取所有图片数据
     * @return 包含所有图片数据的Result对象
     */
    @GetMapping("/images/all")
    public Result<List<Image>> getAllImages() {
        try {
            // 从数据库获取所有图片数据
            List<Image> images = imageRepository.findAll();
            // 返回成功响应，包含图片数据
            return Result.success("获取图片成功", images);
        } catch (Exception e) {
            // 捕获异常并返回错误响应
            return Result.error(500, "获取图片失败: " + e.getMessage());
        }
    }
}