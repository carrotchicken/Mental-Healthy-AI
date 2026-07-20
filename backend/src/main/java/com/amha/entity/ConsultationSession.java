package com.amha.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation_session")
public class ConsultationSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String sessionTitle;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    private Integer messageCount;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段（非表字段）
    @TableField(exist = false)
    private String userNickname;
}
