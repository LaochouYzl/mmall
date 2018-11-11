package com.mmall.dao;

import java.util.List;

import com.mmall.pojo.Category;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);
    
    // 根据parentId获取孩子节点
    List<Category> selectCategoryChildrenByParentId(Integer parentId);
}