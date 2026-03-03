package com.employment.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employment.backend.entity.SysForumComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysForumCommentMapper extends BaseMapper<SysForumComment> {

    // 联查学生表，获取评论人的姓名，按时间升序排列 (老的在上面)
    @Select("SELECT c.*, s.name as commenter_name " +
            "FROM sys_forum_comment c " +
            "LEFT JOIN sys_student s ON c.student_id = s.id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.create_time ASC")
    List<Map<String, Object>> getCommentsByPostId(@Param("postId") Long postId);
}