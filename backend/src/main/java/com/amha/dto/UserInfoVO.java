package com.amha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String phone;
    private Integer gender;
    private String genderDisplayName;
    private Integer userType;
    private String userTypeDisplayName;
    private Integer status;
    private String statusDisplayName;
    private String displayName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
