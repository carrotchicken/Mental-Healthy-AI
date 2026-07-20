package com.amha.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation_message")
public class ConsultationMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Integer senderType;
    private Integer messageType;
    private String content;
    private Integer isError;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
