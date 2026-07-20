package com.amha.controller;

import com.amha.common.Result;
import com.amha.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("businessType") String businessType,
                                              @RequestParam(value = "businessId", required = false) String businessId,
                                              @RequestParam(value = "businessField", required = false) String businessField) {
        return Result.success(fileService.uploadFile(file, businessType, businessId, businessField));
    }
}
