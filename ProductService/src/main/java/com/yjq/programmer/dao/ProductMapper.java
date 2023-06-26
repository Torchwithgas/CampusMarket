package com.yjq.programmer.dao;

import com.yjq.programmer.domain.Product;
import com.yjq.programmer.domain.ProductExample;

import java.util.List;

public interface ProductMapper {
    int countByExample(ProductExample example);

    int deleteByExample(ProductExample example);

    int deleteByPrimaryKey(String id);

    int insertSelective(Product record);

    List<Product> selectByExample(ProductExample example);

    Product selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Product record);

}
