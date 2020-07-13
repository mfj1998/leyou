package com.leyou.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.client.GoodsClient;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.dto.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.IndexService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexServiceImpl implements IndexService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    /**
     * 因为goods需要的信息几乎都包含在SpuDTO的数据库中,只需要在这里将其数据库中的所有信息查询出来，
     * 然后在贴到索引库中
     *
     * @param spuDTO
     * @return
     */
    public Goods buildGoods(SpuDTO spuDTO) {
        //1、获取到id
        Long id = spuDTO.getId();
        //2、获取到sub_Title，卖点
        String subTitle = spuDTO.getSubTitle();

        //3、获取到sku信息的json结构，id、title、images、price
        List<SkuDTO> skuDTOS = goodsClient.querySkuById(spuDTO.getId());
        Set<Long> skuPrice = new HashSet<>();        //这个是价格
        List<Map<String, Object>> skuList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOS) {
            Map<String, Object> map = new HashMap<>();
            //对价格进行处理
            String priceStr = String.valueOf(skuDTO.getPrice());
            String str = priceStr;
            if (str.length() > 2) {
                str = str.substring(0, priceStr.length() - 2) + ".00";
            }
            map.put("id", skuDTO.getId());
            map.put("title", skuDTO.getTitle());
            map.put("image", skuDTO.getImages());
            map.put("price", str);
            skuList.add(map);
            skuPrice.add(skuDTO.getPrice());
        }
        String skuString = JsonUtils.toString(skuList);


        //4、获取到品牌的id
        Long brandId = spuDTO.getBrandId();

        //5、获取到分类的三级分类标题
        List<Long> categoryIds = spuDTO.getCategoryIds();
        Long categoryId = categoryIds.get(2);


        //6、all，名称、分类、品牌、规格信息等
        String name = spuDTO.getName();
        //根据brandId获取到对应的品牌名称
        BrandDTO brandDTO = goodsClient.queryBrandById(brandId);
        //分类
        String categoryNames = goodsClient.queryCategoryById(categoryIds)
                .stream()
                .map(CategoryDTO::getName)
                .collect(Collectors.joining(","));
        StringBuilder sb = new StringBuilder();
        String all = sb.append(name).append(" ").append(brandDTO).append(" ").append(categoryNames).toString();


        //7、获取到创建时间,es中存储的为Long类型的
        long time = spuDTO.getCreateTime().getTime();


        //8、规格参数获取
        Map<String, Object> specs = new HashMap<>();

        //8.1、获取到SpecParam类的规格参数key,使用cid3查询,并且设置条件searching(是否用于搜索)为true
        Long cid3 = spuDTO.getCid3();
        List<SpecParamDTO> specParamDTOS = goodsClient.querySpecParamBySpecGroupId(null, cid3, true);
        //8.2、获取到规格参数的值，是在SpuDetail
        SpuDetailDTO spuDetailDTO = goodsClient.querySpuDetailById(spuDTO.getId());
        //8.3、通用规格参数的值,数据存储的为JSON,将json数据转换为键值对对象(key:Long.class,value:Object.class)
        Map<Long, Object> genericMap = JsonUtils.toMap(spuDetailDTO.getGenericSpec(), Long.class, Object.class);
        //8.4、特有规格参数的值,数据库存储的为JSON,转为对象
        Map<Long, List<String>> specialMap = JsonUtils.nativeRead(spuDetailDTO.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        specParamDTOS
                .forEach(specParamDTO -> {
                    String key = specParamDTO.getName();
                    Object value = null;

                    //判断是否是通用的规格
                    if (specParamDTO.getGeneric()) {
                        value = genericMap.get(specParamDTO.getId());
                    } else {
                        //否则为特殊的规格
                        value = specialMap.get(specParamDTO.getId());
                    }
                    //判断是否是数值类型的,是数值类型的就进行分段
                    if (specParamDTO.getNumeric()) {
                        //是数字类型则进行分段
                        value = segmentation(value, specParamDTO);
                    }
                    specs.put(key, value);
                });


        Goods goods = new Goods();
        goods.setId(id);
        goods.setSubTitle(subTitle);
        goods.setSkus(skuString);
        goods.setAll(all);
        goods.setBrandId(brandId);
        goods.setCategoryId(categoryId);
        goods.setCreateTime(time);
        goods.setPrice(skuPrice);
        goods.setSpecs(specs);
        return goods;
    }

    /**
     * 对传递来的规格参数项根据其对应的规格参数进行分层
     *
     * @param value
     * @param specParamDTO
     * @return
     */
    private String segmentation(Object value, SpecParamDTO specParamDTO) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其他";
        }

        //将该类型转换为double类型
        Double val;
        try {
            val = Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            val = 0.00;
        }

        //判断传递过来的数值的范围
        String result = "其他";
        String[] split = specParamDTO.getSegments().split(",");
        for (String str : split) {
            String[] strings = str.split("-");
            double begin = Double.parseDouble(strings[0]);
            double end = Double.MAX_VALUE;
            if (strings.length == 2) {
                end = Double.parseDouble(strings[1]);
            }

            if (begin <= val && end > val) {
                if (strings.length == 1) {      //5000-  的范围
                    result = strings[0] + specParamDTO.getUnit() + "以上";
                } else if (begin == 0) {        // 0-1000的范围
                    result = strings[1] + specParamDTO.getUnit() + "以下";
                } else {                        // 100-200的范围
                    result = strings[0] + "-" + strings[1] + specParamDTO.getUnit();
                }
                break;
            }
        }
        return result;
    }


    @Override
    public void createIndex(Long id) {
        //因为是商品上架需要在es中添加数据,需要先从数据库中查询出来数据
        SpuDTO spuDTO = goodsClient.querySpuById(id);
        //使用构建Goods的方法,将spuDTO对象构建成Goods
        Goods goods = buildGoods(spuDTO);
        goodsRepository.save(goods);
    }

    @Override
    public void deleteIndex(Long id) {
        goodsRepository.deleteById(id);
    }

}
