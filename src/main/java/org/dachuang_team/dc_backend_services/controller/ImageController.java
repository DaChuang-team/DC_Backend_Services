package org.dachuang_team.dc_backend_services.controller;

import org.dachuang_team.dc_backend_services.common.Result;
import org.dachuang_team.dc_backend_services.domain.PO.ImgPO.*;
import org.dachuang_team.dc_backend_services.domain.VO.MessageVO;
import org.dachuang_team.dc_backend_services.enumeration.ConversationUserRole;
import org.dachuang_team.dc_backend_services.enumeration.SysImagePurpose;
import org.dachuang_team.dc_backend_services.domain.VO.FileUploadVO;
import org.dachuang_team.dc_backend_services.repository.*;
import org.dachuang_team.dc_backend_services.services.IStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private IStorageService storageService;

    @Autowired
    private ProductImageRecordRepository productImageRecordRepository;
    @Autowired
    private UserAvatarRecordRepository userAvatarRecordRepository;
    @Autowired
    private SysImageRepository sysImageRepository;
    @Autowired
    private AIInteractionImgRepository aiInteractionImgRepository;
    @Autowired
    private RefundImgRepository refundImgRepository;
    @Autowired
    private ShopBannerImgRepository shopBannerImgRepository;
    @Autowired
    private AccommodationImgRepository accommodationImgRepository;
    @Autowired
    private ConversationImgRepository conversationImgRepository;

    @PutMapping("/productImgUpload")
    public Result<FileUploadVO> uploadImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);

        // 调用存储服务保存物理文件
        IStorageService.StorageResult result = storageService.uploadByFile(file);

        // 创建并保存图片记录信息
        ProductImg record = new ProductImg();
        record.setUrl(result.getUrl()); // 初始上传时，储存原始图片URL
        record.setPhysicalPath(result.getPhysicalPath());
        record.setCreatedAt(LocalDateTime.now());
        record.setLinked(false); // 初始为未绑定

        productImageRecordRepository.save(record);

        // 构造并返回要求的 DTO
        FileUploadVO response = new FileUploadVO();
        response.setId(record.getId()); //回传id数组以关联
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    //上传用于AI交互的图片，记录上传用户和时间，供后续分析使用，不与商品绑定
    @PutMapping("/AIInteractionImgUpload")
    public Result<FileUploadVO> uploadAIInteractionImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);

        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 调用存储服务保存物理文件
        IStorageService.StorageResult result = storageService.uploadByFile(file);
        AIInteractionImg record = new AIInteractionImg();
        record.setImageUrl(result.getUrl());
        record.setUploadTime(LocalDateTime.now());
        record.setUploadUserId(currentUserId);

        aiInteractionImgRepository.save(record);

        // 构造并返回要求的DTO，前端通过传递url对AI发起图片交互请求
        FileUploadVO response = new FileUploadVO();
        response.setId(record.getImageId());
        response.setUrl(result.getUrl()); //回传URL以关联
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    @PutMapping("/refundEvidenceImgUpload")
    public Result<FileUploadVO> uploadRefundEvidenceImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        IStorageService.StorageResult result = storageService.uploadByFile(file);

        RefundImg record = new RefundImg();
        record.setImageUrl(result.getUrl());
        record.setUploadTime(LocalDateTime.now());
        record.setUploadUserId(currentUserId);
        refundImgRepository.save(record);

        FileUploadVO response = new FileUploadVO();
        response.setId(record.getId()); //回传id数组以关联
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    @PutMapping("/shopBannerImgUpload")
    public Result<FileUploadVO> uploadShopBannerImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);
        IStorageService.StorageResult result = storageService.uploadByFile(file);
        ShopBannerImg record = new ShopBannerImg();
        record.setImgUrl(result.getUrl());
        record.setUploadTime(LocalDateTime.now());
        shopBannerImgRepository.save(record);

        FileUploadVO response = new FileUploadVO();
        response.setId(record.getId());
        response.setUrl(result.getUrl()); //回传URL以关联
        response.setFileName(result.getFileName());
        return Result.success("上传成功", response);
    }

    @PutMapping("/accommodationImgUpload")
    public Result<FileUploadVO> uploadAccommodationImg(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);
        Long currentMerchantId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        IStorageService.StorageResult result = storageService.uploadByFile(file);

        AccommodationImg record = new AccommodationImg();
        record.setUrl(result.getUrl());
        record.setPhysicalPath(result.getPhysicalPath());
        record.setCreatedAt(LocalDateTime.now());
        record.setUploadMerchantId(currentMerchantId);

        accommodationImgRepository.save(record);
        FileUploadVO response = new FileUploadVO();
        response.setId(record.getId());
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());
        return Result.success("上传成功", response);
    }


    @PutMapping("/userAvatarUpload")
    public Result<FileUploadVO> uploadUserAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);

        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 调用存储服务保存物理文件
        IStorageService.StorageResult result = storageService.uploadByFile(file);

        // 创建并保存用户头像记录信息
        UserAvatar record = new UserAvatar();
        record.setAvatarUrl(result.getUrl());
        record.setUploadAt(LocalDateTime.now());
        record.setUserId(currentUserId);
        userAvatarRecordRepository.save(record);

        // 构造并返回要求的 DTO，前端后续调用修改用户信息接口时传回这个URL以便关联
        FileUploadVO response = new FileUploadVO();
        response.setId(record.getId());
        response.setUrl(result.getUrl()); //回传URL以关联
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    @PutMapping("/chatImgUpload")
    public Result<FileUploadVO> uploadChatImg(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) return Result.error(406, "文件不能为空", null);

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return Result.error(401, "用户未登录", null);
            }

            Long senderId;
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                senderId = (Long) principal;
            } else if (principal instanceof Number) {
                senderId = ((Number) principal).longValue();
            } else {
                return Result.error(401, "无效的用户身份信息", null);
            }

            String senderRoleStr = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replaceFirst("^ROLE_", ""))
                    .orElse(null);

            if (senderRoleStr == null) {
                return Result.error(403, "无法识别用户角色", null);
            }

            ConversationUserRole senderRole;
            try {
                senderRole = ConversationUserRole.valueOf(senderRoleStr);
            } catch (IllegalArgumentException e) {
                return Result.error(400, "不支持的用户角色: " + senderRoleStr, null);
            }

            IStorageService.StorageResult result = storageService.uploadByFile(file);

            ConversationImg record = new ConversationImg();
            record.setUploadUserRole(senderRole);
            record.setUploadUserId(senderId);
            record.setImgUrl(result.getUrl());
            record.setUploadAt(LocalDateTime.now());
            conversationImgRepository.save(record);

            FileUploadVO response = new FileUploadVO();
            response.setId(record.getId());
            response.setUrl(result.getUrl());
            response.setFileName(result.getFileName());

            return Result.success("上传成功", response);
        } catch (Exception e) {
            return Result.error(500, "聊天图片上传失败: " + e.getMessage(), null);
        }
    }



    //用户中途取消上传商品时，清理已上传但未绑定的图片记录和物理文件，不可用于修改商品时删除已绑定的图片
    //通过传入的URL找到对应记录，验证未绑定后删除记录和物理文件
    @DeleteMapping("/uploadPurge")
    public Result<String> deleteImg(@RequestParam String url) {
        // 先删数据库记录，再删物理文件
        Optional<ProductImg> record = productImageRecordRepository.findByUrl(url);
        if (record.isEmpty()) return Result.error(404, "图片记录未找到");
        ProductImg productImg = record.get();
        if (productImg.getLinked()) return Result.error(400, "图片已绑定到商品，无法删除");
        storageService.delete(url);
        productImageRecordRepository.delete(productImg);
        return Result.success("图片已清理");
    }

    // 管理员接口，上传系统图片资源用于首页或其他区域展示
    @PutMapping("/sysImgUpload")
    public Result<FileUploadVO> uploadSysImg(
            @RequestParam("file") MultipartFile file,
            @RequestParam("purpose") String purpose) { //purpose参数定义见sysImage实体类注释
        if (file.isEmpty()) return Result.error(406, "文件不能为空", null);
        if(purpose == null || purpose.isEmpty()) return Result.error(400, "用途参数不能为空");
        if(!SysImagePurpose.isValidPurpose(purpose)) return Result.error(400, "无效的用途参数");

        IStorageService.StorageResult result = storageService.uploadByFile(file);

        SysImg record = new SysImg();
        record.setImageUrl(result.getUrl());
        record.setPurpose(purpose);
        record.setImageName(result.getFileName());
        sysImageRepository.save(record);

        FileUploadVO response = new FileUploadVO();
        response.setId(record.getImageId());
        response.setUrl(result.getUrl());
        response.setFileName(result.getFileName());

        return Result.success("上传成功", response);
    }

    //用于获取系统图片资源URL，前端根据用途参数调用此接口获取对应图片URL进行展示
    @GetMapping("/sysImgGet")
    public Result<List<Map<String, Object>>> getSysImg(@RequestParam("purpose") String purpose) {
        if (purpose == null || purpose.isEmpty()) return Result.error(400, "用途参数不能为空");

        List<SysImg> records = sysImageRepository.findByPurpose(purpose);
        if (records.isEmpty()) return Result.error(404, "所属用途的图片未找到");

        List<Map<String, Object>> result = records.stream().map(record -> Map.<String, Object>of(
                "imageId",record.getImageId(),
                "imageUrl", record.getImageUrl(),
                "imageName", record.getImageName()
        )).toList();

        return Result.success("查询成功", result);
    }

    // 管理员接口，删除系统图片资源
    @DeleteMapping("/sysImgDelete")
    public Result<String> deleteSysImg(@RequestParam("imageId") Long imageId) {
        SysImg record = sysImageRepository.findByImageId(imageId);
        if (record == null) return Result.error(404, "图片未找到");

        storageService.delete(record.getImageUrl());
        sysImageRepository.delete(record);

        return Result.success("删除成功", null);
    }

    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority();
    }
}