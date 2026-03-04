package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_enterprise_promotion")
public class SysEnterprisePromotion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createTime;
}