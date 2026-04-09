package org.dachuang_team.dc_backend_services.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结构类
 * 用于规范所有接口的返回格式为 JSON
 * @param <T> 响应数据类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;    // 状态码 (如 200 为成功, 400/500 为失败)
    private String message;  // 提示信息
    private T data;          // 响应数据

    // --- 快捷方法 ---

    /**
     * 成功响应的快捷方法 (带数据)
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应的快捷方法 (不带数据)
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应的快捷方法 (自定义信息和数据)
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 成功响应的快捷方法 (自定义状态码、信息和数据)
    public static <T> Result<T> success(Integer code,String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 失败响应的快捷方法
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败响应的快捷方法 (带数据)
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}
