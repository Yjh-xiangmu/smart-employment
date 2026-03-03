package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_student_profile")
public class SysStudentProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String major;
    private String grade;
    private String phone;
    private String email;
    private String expectedPosition;
    private String expectedCity;
    private String expectedSalary;

    // 不在此处直接返回文件字节给前端，防止数据过大卡顿，单独写下载接口
    private byte[] resumeFile;
    private String resumeName;

    private LocalDateTime updateTime;
}