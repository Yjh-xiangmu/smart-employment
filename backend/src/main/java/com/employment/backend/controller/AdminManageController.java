package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.*;
import com.employment.backend.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/manage")
public class AdminManageController {

    @Autowired
    private SysStudentMapper studentMapper;
    @Autowired
    private SysJobMapper jobMapper;
    @Autowired
    private SysEnterpriseMapper enterpriseMapper;
    @Autowired
    private SysStudentProfileMapper profileMapper;
    @Autowired
    private SysResumeDeliveryMapper deliveryMapper;

    // ================= 1. 学生账号管理 =================
    @GetMapping("/student/list")
    public Result<?> getStudentList() {
        List<SysStudent> students = studentMapper.selectList(new QueryWrapper<SysStudent>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();

        for (SysStudent s : students) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("account", s.getStudentNo());
            map.put("name", s.getName());
            // 如果老数据状态为空，默认为1(正常)
            map.put("status", s.getStatus() != null ? s.getStatus() : 1);
            map.put("createTime", s.getCreateTime());

            // 从档案表里连带查出手机号和邮箱
            QueryWrapper<SysStudentProfile> pq = new QueryWrapper<>();
            pq.eq("student_id", s.getId());
            SysStudentProfile profile = profileMapper.selectOne(pq);
            if (profile != null) {
                map.put("phone", profile.getPhone());
                map.put("email", profile.getEmail());
            } else {
                map.put("phone", "未填写");
                map.put("email", "未填写");
            }
            result.add(map);
        }
        return Result.success(result);
    }

    @PostMapping("/student/changeStatus")
    public Result<?> changeStudentStatus(@RequestBody SysStudent student) {
        SysStudent updateStu = new SysStudent();
        updateStu.setId(student.getId());
        updateStu.setStatus(student.getStatus());
        studentMapper.updateById(updateStu);
        return Result.success(student.getStatus() == 1 ? "账号已解禁" : "账号已封禁", null);
    }

    // ================= 2. 职位招聘管理 =================
    @GetMapping("/job/list")
    public Result<?> getJobList() {
        List<SysJob> jobs = jobMapper.selectList(new QueryWrapper<SysJob>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        // 组装企业名称
        for (SysJob job : jobs) {
            Map<String, Object> map = new HashMap<>();
            map.put("job", job);
            SysEnterprise ent = enterpriseMapper.selectById(job.getEnterpriseId());
            map.put("enterpriseName", ent != null ? ent.getEnterpriseName() : "未知企业");
            result.add(map);
        }
        return Result.success(result);
    }

    @PostMapping("/job/changeStatus")
    public Result<?> changeJobStatus(@RequestBody SysJob job) {
        SysJob updateJob = new SysJob();
        updateJob.setId(job.getId());
        updateJob.setStatus(job.getStatus()); // 0表示强制下架，1表示恢复正常
        jobMapper.updateById(updateJob);
        return Result.success(job.getStatus() == 1 ? "职位已恢复" : "职位已强制下架", null);
    }

    // ================= 3. 学生简历管理 =================
    @GetMapping("/resume/list")
    public Result<?> getResumeList() {
        // 【修复点】：把 create_time 改成 update_time
        List<SysStudentProfile> profiles = profileMapper.selectList(new QueryWrapper<SysStudentProfile>().orderByDesc("update_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysStudentProfile profile : profiles) {
            Map<String, Object> map = new HashMap<>();
            map.put("profile", profile);
            SysStudent student = studentMapper.selectById(profile.getStudentId());
            if (student != null) {
                map.put("studentName", student.getName());
            }
            // 手机号和邮箱是从 profile 里获取
            map.put("phone", profile.getPhone());
            map.put("email", profile.getEmail());

            result.add(map);
        }
        return Result.success(result);
    }

    // ================= 4. 简历投递管理 =================
    @GetMapping("/delivery/list")
    public Result<?> getDeliveryList() {
        List<SysResumeDelivery> deliveries = deliveryMapper.selectList(new QueryWrapper<SysResumeDelivery>().orderByDesc("create_time"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysResumeDelivery d : deliveries) {
            Map<String, Object> map = new HashMap<>();
            map.put("delivery", d);
            SysStudent student = studentMapper.selectById(d.getStudentId());
            map.put("studentName", student != null ? student.getName() : "未知学生");

            SysJob job = jobMapper.selectById(d.getJobId());
            map.put("jobName", job != null ? job.getJobName() : "未知职位");

            SysEnterprise ent = enterpriseMapper.selectById(d.getEnterpriseId());
            map.put("enterpriseName", ent != null ? ent.getEnterpriseName() : "未知企业");

            result.add(map);
        }
        return Result.success(result);
    }
}