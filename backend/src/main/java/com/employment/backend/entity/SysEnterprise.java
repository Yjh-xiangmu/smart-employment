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
}