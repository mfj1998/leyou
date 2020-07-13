package com.leyou.page.service;

import java.util.Map;

public interface PageService {

    /**
     * 查询item页面的所有的数据
     * @param id
     * @return
     */
    Map<String, Object> toItemPage(Long id);

    /**
     * 监听器监听着Item-service,该服务中上传了商品,那么触发创建静态页面模板
     * @param id
     */
    void createPage(Long id);

    /**
     * 使用rabbitMQ删除该页面
     * @param id
     */
    void deletePage(Long id);

}
