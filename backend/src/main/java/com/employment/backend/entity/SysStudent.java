package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_student")
public class SysStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentNo;
    private String password;
    private String name;
    private Integer status;
    private LocalDateTime createTime;
}