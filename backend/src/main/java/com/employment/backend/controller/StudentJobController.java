package com.employment.backend.controller;

import com.employment.backend.common.Result;
import com.employment.backend.mapper.SysJobMapper;
import com.employment.backend.mapper.SysEnterprisePromotionMapper; // 新增引入
import com.employment.backend.entity.SysEnterprisePromotion; // 新增引入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.employment.backend.entity.SysEnterprise;
import com.employment.backend.entity.SysJob;
import com.employment.backend.mapper.SysEnterpriseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/job")
public class StudentJobController {

    @Autowired
    private SysJobMapper sysJobMapper;

    @Autowired
    private SysEnterpriseMapper sysEnterpriseMapper;

    @Autowired
    private SysEnterprisePromotionMapper sysEnterprisePromotionMapper; // 注入宣传简章的Mapper

    // 获取岗位列表 (支持 关键词、城市、分类 的动态筛选)
    @GetMapping("/search")
    public Result<?> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category) {

        List<Map<String, Object>> jobs = sysJobMapper.searchStudentJobs(keyword, city, category);
        return Result.success(jobs);
    }

    // 2. 获取企业主页详情 (包含企业基本信息 + 宣传信息 + 该企业的所有在招职位)
    @GetMapping("/company/{enterpriseId}")
    public Result<?> getCompanyDetail(@PathVariable Long enterpriseId) {
        // 1. 获取企业基本信息
        SysEnterprise enterprise = sysEnterpriseMapper.selectById(enterpriseId);
        if (enterprise != null) {
            enterprise.setPassword(null); // 脱敏
            enterprise.setLicenseImage(null); // 营业执照属于隐私，不在前端展示
        }

        // 2. 获取该企业发布的所有正在招聘的岗位
        QueryWrapper<SysJob> jobQuery = new QueryWrapper<>();
        jobQuery.eq("enterprise_id", enterpriseId)
                .eq("status", 1) // 仅展示招聘中的
                .orderByDesc("create_time");
        List<SysJob> jobs = sysJobMapper.selectList(jobQuery);

        // 3. 【新增】获取该企业发布的所有宣传简章
        QueryWrapper<SysEnterprisePromotion> promoQuery = new QueryWrapper<>();
        promoQuery.eq("enterprise_id", enterpriseId)
                .orderByDesc("create_time");
        List<SysEnterprisePromotion> promotions = sysEnterprisePromotionMapper.selectList(promoQuery);

        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("company", enterprise);
        result.put("jobs", jobs);
        result.put("promotions", promotions); // 将宣传简章加入返回结果

        return Result.success(result);
    }
}