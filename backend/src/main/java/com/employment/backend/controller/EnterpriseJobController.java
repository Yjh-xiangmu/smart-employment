package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysJob;
import com.employment.backend.mapper.SysJobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/enterprise/job")
public class EnterpriseJobController {

    @Autowired
    private SysJobMapper sysJobMapper;

    // 1. 发布新职位
    @PostMapping("/add")
    public Result<?> addJob(@RequestBody SysJob job) {
        job.setStatus(1); // 默认发布即为“招聘中”
        sysJobMapper.insert(job);
        return Result.success("职位发布成功！", null);
    }

    // 2. 获取当前企业的所有职位列表
    @GetMapping("/list/{enterpriseId}")
    public Result<?> getJobList(@PathVariable Long enterpriseId) {
        QueryWrapper<SysJob> query = new QueryWrapper<>();
        query.eq("enterprise_id", enterpriseId);
        query.orderByDesc("create_time"); // 按时间倒序，最新的在前面
        List<SysJob> list = sysJobMapper.selectList(query);
        return Result.success(list);
    }

    // 3. 修改职位信息
    @PostMapping("/update")
    public Result<?> updateJob(@RequestBody SysJob job) {
        sysJobMapper.updateById(job);
        return Result.success("职位修改成功！", null);
    }

    // 4. 上下架职位 (切换 status)
    @PostMapping("/changeStatus")
    public Result<?> changeStatus(@RequestBody SysJob job) {
        SysJob updateJob = new SysJob();
        updateJob.setId(job.getId());
        updateJob.setStatus(job.getStatus());
        sysJobMapper.updateById(updateJob);

        String msg = job.getStatus() == 1 ? "职位已重新上架！" : "职位已下架！";
        return Result.success(msg, null);
    }
    // 5. 获取当前企业使用过的所有职位分类 (去重)
    @GetMapping("/categories/{enterpriseId}")
    public Result<?> getJobCategories(@PathVariable Long enterpriseId) {
        QueryWrapper<SysJob> query = new QueryWrapper<>();
        // 使用 DISTINCT 关键字去重查询
        query.select("DISTINCT category")
                .eq("enterprise_id", enterpriseId)
                .isNotNull("category");

        List<SysJob> list = sysJobMapper.selectList(query);

        // 提取出纯字符串列表
        List<String> categories = list.stream()
                .map(SysJob::getCategory)
                .collect(Collectors.toList());
        return Result.success(categories);
    }
}