package com.rentalcar.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${image.directory}")
    private String userImageDir;

    @Override
    public String storeFile(MultipartFile file, String path) {
        String fileExtension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        final Path fileStorageLocation = Path.of(userImageDir, path, (Instant.now().toEpochMilli())+fileExtension);

        if (file.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "image must not be blank");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files uploaded must be images");
        }

        try {
            Files.createDirectories(fileStorageLocation.getParent());
            Files.copy(file.getInputStream(), fileStorageLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileStorageLocation.toString().replace("\\", "/");
        }catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "error upload image");
        }
    }

    @Override
    public void deleteFile(String path) {
        Path pathImage = Path.of(path);
        try {
            boolean b = Files.deleteIfExists(pathImage);
            System.out.println("Status delete => " + b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
