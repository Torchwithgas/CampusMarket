package com.yjq.programmer.feignclient;

import com.yjq.programmer.dto.ProductDTO;
import com.yjq.programmer.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("ProductService")
public interface ProductClient {

    @PostMapping("/product/store/get")
    ResponseDTO<List<ProductDTO>> getByStore(@RequestBody ProductDTO productDTO);

    @PostMapping("/product/get")
    ResponseDTO<ProductDTO> getById(@RequestBody ProductDTO productDTO);

    @PostMapping("/product/save")
    ResponseDTO<Boolean> saveProduct(@RequestBody ProductDTO productDTO);

    @PostMapping("/product/remove/store_id")
    ResponseDTO<Boolean> removeProductByStoreId(@RequestBody ProductDTO productDTO);
}
