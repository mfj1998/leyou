package com.leyou.item.service.impl;

import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryDTO> findByParentId(Long pid) {
        Category category = new Category();
        category.setParentId(pid);

        List<Category> categoryList = categoryMapper.select(category);
        /*if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
*/
        //使用自定义的工具类进行对集合类型的转换，BeanUtils.copyProperties()方法,既是一个copy的副本数据的方法,但是copy的两个类的属性名一定要相同
        return BeanHelper.copyWithCollection(categoryList,CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> queryCategoryByBrandId(Long bid) {
        List<CategoryDTO> categoryDTOS = categoryMapper.queryCategoryByBrandId(bid);
        /*if (CollectionUtils.isEmpty(categoryDTOS)) {
            throw new LyException(ExceptionEnum.QUERY_NOT_FOUND);
        }*/
        return categoryDTOS;
    }

    @Override
    public List<CategoryDTO> queryCategorys(List<Long> categoryIds) {
        List<Category> categoryList = categoryMapper.selectByIdList(categoryIds);
        return BeanHelper.copyWithCollection(categoryList,CategoryDTO.class);
    }

}
