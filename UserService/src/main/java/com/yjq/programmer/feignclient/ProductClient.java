package com.yjq.programmer.feignclient;

import com.yjq.programmer.dto.ProductDTO;
import com.yjq.programmer.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("ProductService")
public interface ProductClient {

    @PostMapping("/product/get")
    ResponseDTO<ProductDTO> getById(@RequestBody ProductDTO productDTO);
}
