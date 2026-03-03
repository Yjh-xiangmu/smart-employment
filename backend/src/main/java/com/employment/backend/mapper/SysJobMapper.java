package com.employment.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employment.backend.entity.SysJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysJobMapper extends BaseMapper<SysJob> {

    // 编写自定义动态 SQL，联查企业表获取 enterprise_name，且只查询 status = 1 (招聘中) 的岗位
    @Select("<script>" +
            "SELECT j.*, e.enterprise_name " +
            "FROM sys_job j " +
            "LEFT JOIN sys_enterprise e ON j.enterprise_id = e.id " +
            "WHERE j.status = 1 " +
            "<if test='keyword != null and keyword != \"\"'> " +
            "   AND (j.job_name LIKE CONCAT('%',#{keyword},'%') OR e.enterprise_name LIKE CONCAT('%',#{keyword},'%')) " +
            "</if>" +
            "<if test='city != null and city != \"\"'> AND j.city = #{city} </if>" +
            "<if test='category != null and category != \"\"'> AND j.category = #{category} </if>" +
            "ORDER BY j.create_time DESC" +
            "</script>")
    List<Map<String, Object>> searchStudentJobs(@Param("keyword") String keyword,
                                                @Param("city") String city,
                                                @Param("category") String category);
}