package com.yjq.programmer.service;

import com.yjq.programmer.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ISystemService {

    ResponseDTO<String> uploadPhoto(MultipartFile photo);

    ResponseEntity<?> viewPhoto(String filename);
}
