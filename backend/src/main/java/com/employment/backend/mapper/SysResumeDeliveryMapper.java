package com.employment.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employment.backend.entity.SysResumeDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysResumeDeliveryMapper extends BaseMapper<SysResumeDelivery> {

    // ================== 学生端专属查询 ==================
    // 联合查询：投递记录 + 职位信息 + 企业名称
    @Select("SELECT d.id AS delivery_id, d.status, d.create_time, " +
            "j.job_name, j.city, j.salary, " +
            "e.enterprise_name " +
            "FROM sys_resume_delivery d " +
            "LEFT JOIN sys_job j ON d.job_id = j.id " +
            "LEFT JOIN sys_enterprise e ON d.enterprise_id = e.id " +
            "WHERE d.student_id = #{studentId} " +
            "ORDER BY d.create_time DESC")
    List<Map<String, Object>> getStudentDeliveryList(@Param("studentId") Long studentId);


    // ================== 企业端专属查询 ==================
    // 联合查询：收到的简历记录 + 职位名称 + 学生姓名 + 学生档案信息 (排除已取消的)
    @Select("SELECT d.id AS delivery_id, d.status, d.create_time, " +
            "j.job_name, " +
            "s.name AS student_name, " +
            "p.major, p.grade, p.phone, p.email, p.student_id " +
            "FROM sys_resume_delivery d " +
            "LEFT JOIN sys_job j ON d.job_id = j.id " +
            "LEFT JOIN sys_student s ON d.student_id = s.id " +
            "LEFT JOIN sys_student_profile p ON d.student_id = p.student_id " +
            "WHERE d.enterprise_id = #{enterpriseId} AND d.status != 4 " +
            "ORDER BY d.create_time DESC")
    List<Map<String, Object>> getEnterpriseDeliveryList(@Param("enterpriseId") Long enterpriseId);

}