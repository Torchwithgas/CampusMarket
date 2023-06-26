package com.yjq.programmer.dao.my;

import com.yjq.programmer.dto.CategoryDTO;

import java.util.List;


public interface MyCategoryMapper {

    // 根据销售金额获取前五个销售金额最高的商品分类
    List<CategoryDTO> getCategoryListByPrice();
}
