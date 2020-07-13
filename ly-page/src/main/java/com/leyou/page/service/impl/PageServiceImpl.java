package com.leyou.page.service.impl;

import com.leyou.client.GoodsClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private GoodsClient goodsClient;
    //生成静态页面的模板方法
    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${ly.static.itemDir}")
    private String itemDir;
    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;

    @Override
    public Map<String, Object> toItemPage(Long spuId) {
        //查询spu的信息
        SpuDTO spuDTO = goodsClient.querySpuById(spuId);
        //根据分类id的集合查询所有商品分类
        List<CategoryDTO> categoryDTOS = goodsClient.queryCategoryById(spuDTO.getCategoryIds());
        //根据品牌id查询品牌
        BrandDTO brandDTO = goodsClient.queryBrandById(spuDTO.getBrandId());
        //根据分类id查询规格参数组等等
        Long cid3 = spuDTO.getCid3();
        List<SpecGroupDTO> specGroupDTOS = goodsClient.querySpecGroupOfParamsById(cid3);

        Map<String,Object> map = new HashMap<>();
        map.put("categories", categoryDTOS);
        map.put("brand", brandDTO);
        map.put("spuName", spuDTO.getName());
        map.put("subTitle", spuDTO.getSubTitle());
        map.put("skus", spuDTO.getSkus());
        map.put("detail", spuDTO.getSpuDetail());
        map.put("specs", specGroupDTOS);
        return map;
    }

    public void createItemHtml(Long id) {
        //上下文域
        Context context = new Context();
        //调用查询静态页面的方法，将其放入到上下文中
        context.setVariables(toItemPage(id));
        File file = new File(itemDir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                // 创建失败，抛出异常
                log.error("【静态页服务】创建静态页目录失败，目录地址：{}", file.getAbsolutePath());
                throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
            }
        }
        File filePath = new File(file, id + ".html");
        // 准备输出流
        try (PrintWriter writer = new PrintWriter(filePath, "UTF-8")) {
            templateEngine.process(itemTemplate, context, writer);
        } catch (IOException e) {
            log.error("【静态页服务】静态页生成失败，商品id：{}", id, e);
            throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }
    }

    @Override
    public void createPage(Long id) {
        createItemHtml(id);
    }

    /**
     * 删除静态页面
     * @param id
     */
    @Override
    public void deletePage(Long id) {
        File file = new File(itemDir,id+".html");
        if (file.exists()) {
            boolean flag = file.delete();
            if (!flag) {
                throw new LyException(ExceptionEnum.FILE_DELETE_ERROR);
            }
        }
    }

    public List<SpuDTO> querySpuBySaleable() {
        return goodsClient.querySpuBySaleable(true);
    }
}
