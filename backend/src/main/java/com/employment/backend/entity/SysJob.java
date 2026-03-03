package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_job")
public class SysJob {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String jobName;
    private String category;
    private String city;
    private String salary;
    private String education;
    private String experience;
    private String description;
    private Integer status; // 1: 招聘中, 0: 已下架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}