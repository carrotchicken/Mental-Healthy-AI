package com.amha.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_upload")
public class FileUpload {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String businessType;
    private String businessId;
    private String businessField;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
