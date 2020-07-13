package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用来设置动态响应码和信息的
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_QUERY(204,"查询的商品为空"),
    DATA_TRANSFER_ERROR(500,"服务器内部数据类型转换的异常"),
    BRAND_NOT_FOUND(204,"查询的品牌为空"),
    BRAND_INSERT_FAILURE(500,"品牌信息添加失败"),
    INVALID_FILE_TYPE(406,"无效的文件名"),
    FILE_UPLOAD_FAILURE(417,"文件上传失败"),
    FILE_IS_NULL(204,"上传的文件为空"),
    FILE_SAVE_FAILURE(204,"文件保存失败"),
    UPDATE_DATA_FAILURE(500,"数据修改失败"),
    QUERY_NOT_FOUND(204,"该数据为空"),
    DATA_DELETE_FAILURE(500,"数据删除失败"),
    DATA_SAVE_FAILURE(500,"数据保存失败"),
    SERVER_ERROR(500,"服务器内部异常"),
    INVALID_PARAM_ERROR(400,"请求的参数不能为空"),
    DIRECTORY_WRITER_ERROR(500,"文件写入失败"),
    FILE_WRITER_ERROR(500,"文件读写失败"),
    FILE_DELETE_ERROR(500,"文件删除失败"),
    SEND_MESSAGE_ERROR(500,"短信发送失败"),
    PARAM_ERROR(400,"请求的参数异常"),
    USER_TOKEN_ERROR(401,"该用户登录信息已经失效"),
    SECURITY_ERROR(400,"无权限访问"),
    TOEKN_MESSAGE_ERROR(401,"TOKEN信息解析失败"),
    CART_IS_NULL(204,"购物车为空"),
    ORDER_PAY_ERROR(500,"订单支付失败")
    ;
    private int status;
    private String message;
}
