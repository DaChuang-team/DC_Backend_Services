package org.dachuang_team.dc_backend_services.pojo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class fileUploadResponseDTO {
    private String url;      // 给前端显示的预览地址
    private String fileName; // 文件名

    public fileUploadResponseDTO() {

    }
}
