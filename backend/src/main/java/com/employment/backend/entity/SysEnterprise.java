package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_enterprise")
public class SysEnterprise {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String account;
    private String password;
    private String enterpriseName;
    private String legalPerson;

    // 这里非常关键！LONGBLOB 对应 Java 的 byte[] 字节数组
    private byte[] licenseImage;

    private Integer status; // 0待审, 1通过, 2驳回
    private LocalDateTime createTime;
    // --- 新增字段 ---
    private String address;        // 企业地址
    private String industry;       // 所属行业
    private String scale;          // 企业规模
    private String contactPerson;  // 联系人
    private String contactPhone;   // 联系电话
    private String description;    // 企业简介
}