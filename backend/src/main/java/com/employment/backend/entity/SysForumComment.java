package com.employment.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_forum_comment")
public class SysForumComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long studentId;
    private String content;
    private LocalDateTime createTime;
}