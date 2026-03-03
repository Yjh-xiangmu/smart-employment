package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_resume_delivery")
public class SysResumeDelivery {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long enterpriseId;
    private Long jobId;
    private Integer status; // 0:待处理, 1:已查看, 2:邀约面试, 3:不合适, 4:已取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}