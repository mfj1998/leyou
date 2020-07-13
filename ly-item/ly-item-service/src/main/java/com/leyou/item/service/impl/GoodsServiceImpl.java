package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.entity.*;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    //查询商品分类
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageResult<SpuDTO> queryBrandPage(Integer page, Integer rows, String key, Boolean saleable) {
        PageHelper.startPage(page, rows);

        //1、构建动态SQL
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%");
        }

        //对上下架商品进行过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        example.setOrderByClause("update_time DESC");

        //执行
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }

        //转换、获取结果
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(spus, SpuDTO.class);
        saveBrandNameAndCategoryName(spuDTOS);

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), spuDTOS);
    }

    /**
     * 查询出SPU对应的category的商品分类和品牌对应的name
     *
     * @param spuDTOS
     */
    public void saveBrandNameAndCategoryName(List<SpuDTO> spuDTOS) {
        for (SpuDTO spuDTO : spuDTOS) {
            Long brandId = spuDTO.getBrandId();

            //查询商品分类，属于多个分类;一个商品属于一个品牌,一个品牌属于多个分类
            List<Long> categoryIds = spuDTO.getCategoryIds();
            String categoryNames =
                    categoryService.queryCategorys(categoryIds)
                            .stream()
                            .map(CategoryDTO::getName)
                            .collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryNames);

            //根据brand_id查询出来商品品牌
            Brand brand = brandService.queryById(brandId);
            spuDTO.setBrandName(brand.getName());
        }
    }


    @Override
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        List<Sku> skuList = BeanHelper.copyWithCollection(spuDTO.getSkus(), Sku.class);

        //1、保存spu的信息
        int count = spuMapper.insertSelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_SAVE_FAILURE);
        }

        //2、保存SpuDetail的信息
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insertSelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_SAVE_FAILURE);
        }

        //3、保存List<SkuDTO>
        if (!CollectionUtils.isEmpty(skuList)) {
            for (Sku sku : skuList) {
                sku.setSpuId(spu.getId());
            }
            count = skuMapper.insertList(skuList);
            if (count != skuList.size()) {
                throw new LyException(ExceptionEnum.DATA_SAVE_FAILURE);
            }
        }


    }

    //TODO 当将商品进行下架的时候,需要将mysql、es、page静态页面进行修改
    @Override
    @Transactional
    public void updateGoodsSaleable(Long sid, Boolean saleable) {
        Spu spu = new Spu();
        spu.setId(sid);
        spu.setSaleable(saleable);
        spu.setUpdateTime(new Date());

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }

        //注意，因为spu的saleable字段适合sku的enable关联的，所以spu修改时这个也需要修改
        //根据sid去查询出来
        Sku skuInfo = new Sku();
        skuInfo.setSpuId(sid);
        List<Sku> skus = skuMapper.select(skuInfo);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }

        //修改sku的操作
        count = 0;
        for (Sku sku : skus) {
            sku.setEnable(saleable);
            sku.setUpdateTime(spu.getUpdateTime());
            int num = skuMapper.updateSkuBySpuId(sku);
            count += num;
        }

/*        if (count != skus.size()) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }*/

        //使用rabbitMQ进行生产消息;为true调用上架的路由,为false调用下架的路由
        String key = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;
        //参数1：交换机名字  参数2：路由   参数3：发送的消息
        amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME, key, sid);
    }


    @Override
    public SpuDetailDTO querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    @Override
    public List<SkuDTO> querySkuById(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);
        return BeanHelper.copyWithCollection(skuList, SkuDTO.class);
    }

    @Override
    @Transactional
    public void updateGoods(SpuDTO spuDTO) {
        //类型转换
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        List<Sku> skuList = Collections.synchronizedList(BeanHelper.copyWithCollection(spuDTO.getSkus(), Sku.class));
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);


        //修改spu的操作,根据主键进行修改
        spu.setUpdateTime(new Date());
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }


        //根据spuId修改spuDetail的信息
        count = spuDetailMapper.updateByPrimaryKey(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }

        //修改List<Sku>,先删除,再增加新的,因为这个集合中的Sku对象是没有skuId的,需要手动去添加
        Sku skuOne = new Sku();
        skuOne.setSpuId(spu.getId());
        int skuCount = skuMapper.selectCount(skuOne);
        skuList.forEach(sku -> sku.setSpuId(spu.getId()));
        if (!CollectionUtils.isEmpty(skuList)) {
            count = skuMapper.deleteBySpuId(spuDetail.getSpuId());
            if (skuCount != count) {
                throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
            }
        }
        //重新添加sku
        count = skuMapper.insertList(skuList);
        if (count != skuList.size()) {
            throw new LyException(ExceptionEnum.DATA_SAVE_FAILURE);
        }

    }

    @Override
    @Transactional
    public void deleteGoods(Long sid) {
        int count = spuMapper.deleteByPrimaryKey(sid);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }

        SpuDetail spuDetail = new SpuDetail();
        spuDetail.setSpuId(sid);
        count = spuDetailMapper.delete(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }

        Sku sku = new Sku();
        sku.setSpuId(sid);
        int selectCount = skuMapper.selectCount(sku);
        count = skuMapper.deleteBySpuId(sid);
        if (count != selectCount) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }
    }

    @Override
    public List<CategoryDTO> getClassification(Long cid) {
        Spu spu = new Spu();
        spu.setCid3(cid);
        List<Spu> spuList = spuMapper.select(spu);
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        Spu spuInfo = spuList.get(0);
        Long cid3 = spuInfo.getCid3();
        if (cid3 == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        Long cid2 = spuInfo.getCid2();
        if (cid2 == null) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }
        Long cid1 = spuInfo.getCid1();
        if (cid1 == null) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOS = categoryService.queryCategorys(Arrays.asList(cid1, cid2, cid3));
        return categoryDTOS;
    }

    @Override
    public SpuDTO querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        spuDTO.setSpuDetail(querySpuDetailById(id));
        spuDTO.setSkus(querySkuById(id));
        return spuDTO;
    }

    @Override
    public List<SpuDTO> querySpuBySaleable(Boolean saleable) {
        Spu spu = new Spu();
        spu.setSaleable(saleable);
        List<Spu> spuList = spuMapper.select(spu);
        if (CollectionUtils.isEmpty(spuList)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(spuList, SpuDTO.class);
    }

    @Override
    public List<SkuDTO> querySkuByIdList(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skuList, SkuDTO.class);
    }

    @Override
    @Transactional
    public void updateSkuStock(Map<Long, Integer> cartMap) {
        cartMap.entrySet().stream()
                .forEach(longIntegerEntry -> {
                    Long skuId = longIntegerEntry.getKey();
                    Integer num = longIntegerEntry.getValue();
                    int count = skuMapper.updateSkuStock(skuId,num);
                    if (count != 1) {
                        throw new LyException(ExceptionEnum.ORDER_PAY_ERROR);
                    }
                });
    }
}
