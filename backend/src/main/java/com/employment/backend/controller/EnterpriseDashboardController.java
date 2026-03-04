package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysJob;
import com.employment.backend.entity.SysResumeDelivery;
import com.employment.backend.mapper.SysJobMapper;
import com.employment.backend.mapper.SysResumeDeliveryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/enterprise/dashboard")
public class EnterpriseDashboardController {

    @Autowired
    private SysJobMapper sysJobMapper;

    @Autowired
    private SysResumeDeliveryMapper deliveryMapper;

    @GetMapping("/stats/{enterpriseId}")
    public Result<?> getStats(@PathVariable Long enterpriseId) {
        Map<String, Object> stats = new HashMap<>();

        // 1. 获取当前在招职位数 (status = 1)
        QueryWrapper<SysJob> jobQuery = new QueryWrapper<>();
        jobQuery.eq("enterprise_id", enterpriseId).eq("status", 1);
        long activeJobs = sysJobMapper.selectCount(jobQuery);
        stats.put("activeJobs", activeJobs);

        // 2. 收到简历总数 (排除学生主动取消的 status = 4)
        QueryWrapper<SysResumeDelivery> deliveryQuery = new QueryWrapper<>();
        deliveryQuery.eq("enterprise_id", enterpriseId).ne("status", 4);
        long totalResumes = deliveryMapper.selectCount(deliveryQuery);
        stats.put("totalResumes", totalResumes);

        // 3. 待处理简历数 (status = 0)
        QueryWrapper<SysResumeDelivery> pendingQuery = new QueryWrapper<>();
        pendingQuery.eq("enterprise_id", enterpriseId).eq("status", 0);
        long pendingResumes = deliveryMapper.selectCount(pendingQuery);
        stats.put("pendingResumes", pendingResumes);

        // 4. 已发邀约数 (status = 2)
        QueryWrapper<SysResumeDelivery> inviteQuery = new QueryWrapper<>();
        inviteQuery.eq("enterprise_id", enterpriseId).eq("status", 2);
        long inviteCount = deliveryMapper.selectCount(inviteQuery);
        stats.put("inviteCount", inviteCount);

        return Result.success(stats);
    }
}