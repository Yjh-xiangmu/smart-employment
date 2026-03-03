package com.employment.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employment.backend.entity.SysForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysForumPostMapper extends BaseMapper<SysForumPost> {

    // 动态SQL：联查学生表获取发帖人姓名，并支持关键词搜索
    @Select("<script>" +
            "SELECT f.*, s.name as author_name " +
            "FROM sys_forum_post f " +
            "LEFT JOIN sys_student s ON f.student_id = s.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (f.title LIKE CONCAT('%', #{keyword}, '%') OR f.content LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY f.create_time DESC" +
            "</script>")
    List<Map<String, Object>> getForumList(@Param("keyword") String keyword);
}