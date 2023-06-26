package com.yjq.programmer.dao;

import com.yjq.programmer.domain.Category;
import com.yjq.programmer.domain.CategoryExample;

import java.util.List;

public interface CategoryMapper {

    int deleteByPrimaryKey(String id);


    int insertSelective(Category record);

    List<Category> selectByExample(CategoryExample example);

    Category selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Category record);

}
