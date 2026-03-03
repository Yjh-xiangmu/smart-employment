package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_admin")
public class SysAdmin {
    // 对应主键，使用数据库自增策略
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}