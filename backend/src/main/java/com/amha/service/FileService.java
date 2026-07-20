package com.amha.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface FileService {
    Map<String, String> uploadFile(MultipartFile file, String businessType,
                                   String businessId, String businessField);
}
