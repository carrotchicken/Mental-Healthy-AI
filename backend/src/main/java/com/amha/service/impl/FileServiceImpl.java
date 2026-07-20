package com.amha.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.amha.common.BusinessException;
import com.amha.entity.FileUpload;
import com.amha.mapper.FileUploadMapper;
import com.amha.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.allowed-extensions:jpg,jpeg,png,gif,webp,pdf,doc,docx}")
    private String allowedExtensions;

    @Override
    public Map<String, String> uploadFile(MultipartFile file, String businessType,
                                          String businessId, String businessField) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = FileUtil.extName(originalFilename);
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedExtensions.split(",")));
        if (!allowed.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件格式: " + ext);
        }

        String newFilename = IdUtil.fastSimpleUUID() + "." + ext;
        String dateDir = cn.hutool.core.date.DateUtil.format(new Date(), "yyyy/MM/dd");
        File destDir = new File(uploadPath, dateDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File destFile = new File(destDir, newFilename);
        try {
            file.transferTo(destFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException("文件保存失败: " + e.getMessage());
        }

        String relativePath = dateDir + "/" + newFilename;

        FileUpload record = new FileUpload();
        record.setFileName(originalFilename);
        record.setFilePath(relativePath);
        record.setFileSize(file.getSize());
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setBusinessField(businessField);
        fileUploadMapper.insert(record);

        Map<String, String> result = new HashMap<>();
        result.put("filePath", "/file/uploads/" + relativePath);
        return result;
    }

    private final FileUploadMapper fileUploadMapper;
}
