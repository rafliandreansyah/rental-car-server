package com.rentalcar.server.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, String path);

    void deleteFile(String path);

}
