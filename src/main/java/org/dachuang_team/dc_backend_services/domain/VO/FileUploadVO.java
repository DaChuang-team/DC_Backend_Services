package org.dachuang_team.dc_backend_services.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadVO {
    private Long id;         // 数据库中存储的图片ID
    private String url;      // 给前端显示的预览地址
    private String fileName; // 文件名

    public FileUploadVO() {

    }
}
