package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);

        //过滤条件，模糊查询  Example是动态sql构建器,相当于添加了where条件
        Example example = new Example(Brand.class);
        if (StringUtils.isNoneBlank(key)) {
            //添加条件：     Criteria是Example的一个内部类,是动态SQL的生成器,可以生成and、or、between等等
            example.createCriteria()
                    .orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }

        //排序,是空默认的升序,不为空则降序
        if (StringUtils.isNoneBlank(sortBy)) {                          //注意：这里用的是common.lang3包
            //判断是否允许降序排序,允许则添加条件
            String orderByClaus = sortBy + (desc ? "DESC" : " ASC");    //这里记住,一定要加空格,不然会拼接为 byASC
            example.setOrderByClause(orderByClaus);
        }

        //执行查询的操作,查询没空返回204
        List<Brand> brandList = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        //将得到的结果进行复制转换
        List<BrandDTO> brandDTOList = BeanHelper.copyWithCollection(brandList, BrandDTO.class);

        //解析分页的结果,包含了分页得到的详细信息(总页数、每页数量等)
        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        return new PageResult<>(pageInfo.getTotal(),brandDTOList);
    }


    @Override
    @Transactional      //开启事务,保证原子性
    public void saveBrand(BrandDTO brandDTO, List<Long> ids) {
        //这里保存的为品牌的基本信息
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        int insertCount = brandMapper.insert(brand);
        if (insertCount == 0) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_FAILURE);
        }

        //这里保存的为品牌和类别的中间表
        insertCount = brandMapper.insertBrandIdAndCategoryId(brand.getId(),ids);
        if (insertCount == 0) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_FAILURE);
        }
    }

    @Override
    @Transactional
    public void updateBrand(BrandDTO brandDTO, List<Long> cids) {
        //1、先修改品牌的基本信息
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        brand.setUpdateTime(new Date());

        //
        int count = brandMapper.updateBrand(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_DATA_FAILURE);
        }

        //3、先将该品牌的id中间表关系进行分删除,在重构表关系
        count = categoryMapper.deleteBrandAndCategory(brand.getId());

        count = brandMapper.insertBrandIdAndCategoryId(brand.getId(),cids);
        if (count != cids.size()) {
            throw new LyException(ExceptionEnum.BRAND_INSERT_FAILURE);
        }
    }

    @Override
    @Transactional
    public void deleteBrand(Long bid) {
        //1、删除品牌信息
        Brand brand = new Brand();
        brand.setId(bid);
        int count = brandMapper.delete(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }

        //2、删除中间表的信息
        int total = categoryMapper.queryCount(bid);
        count = categoryMapper.deleteBrandAndCategory(bid);
        if (total != count) {
            throw new LyException(ExceptionEnum.DATA_DELETE_FAILURE);
        }

    }

    @Override
    public Brand queryById(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        return brand;
    }

    @Override
    public List<BrandDTO> queryBrandByCategoryId(Long cid) {
        List<Brand> brandList = brandMapper.queryBrandByCategoryId(cid);
        return BeanHelper.copyWithCollection(brandList, BrandDTO.class);
    }

    @Override
    public BrandDTO queryBrandById(Long bid) {
        Brand brand = brandMapper.selectByPrimaryKey(bid);
        return BeanHelper.copyProperties(brand,BrandDTO.class);
    }

    @Override
    public List<BrandDTO> queryBrandByIds(List<Long> ids) {
        List<Brand> brandList = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brandList,BrandDTO.class);
    }
}
