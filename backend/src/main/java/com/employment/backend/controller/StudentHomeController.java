package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysNews;
import com.employment.backend.mapper.SysJobMapper;
import com.employment.backend.mapper.SysNewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/home")
public class StudentHomeController {

    @Autowired
    private SysNewsMapper sysNewsMapper;

    @Autowired
    private SysJobMapper sysJobMapper;

    @GetMapping("/data")
    public Result<?> getHomeData() {
        Map<String, Object> data = new HashMap<>();

        // 1. 获取最新5条新闻资讯 (type=1)
        QueryWrapper<SysNews> newsQuery = new QueryWrapper<>();
        newsQuery.eq("type", 1).orderByDesc("create_time").last("LIMIT 5");
        data.put("newsList", sysNewsMapper.selectList(newsQuery));

        // 2. 获取最新5条就业政策 (type=2)
        QueryWrapper<SysNews> policyQuery = new QueryWrapper<>();
        policyQuery.eq("type", 2).orderByDesc("create_time").last("LIMIT 5");
        data.put("policyList", sysNewsMapper.selectList(policyQuery));

        // 3. 获取最新8个热门岗位 (状态为招聘中)
        // 复用之前写好的 searchStudentJobs 联查方法，传 null 代表查全部
        List<Map<String, Object>> allJobs = sysJobMapper.searchStudentJobs(null, null, null);
        // 只截取最新的8个展示在首页
        List<Map<String, Object>> hotJobs = allJobs.size() > 8 ? allJobs.subList(0, 8) : allJobs;
        data.put("hotJobs", hotJobs);

        return Result.success(data);
    }
}