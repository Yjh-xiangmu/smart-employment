package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysAdmin;
import com.employment.backend.service.ISysAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.employment.backend.entity.SysEnterprise;
import com.employment.backend.mapper.SysEnterpriseMapper;
import cn.hutool.core.codec.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ISysAdminService sysAdminService;

    // 管理员登录接口
    @PostMapping("/login")
    public Result<?> login(@RequestBody SysAdmin admin) {
        QueryWrapper<SysAdmin> query = new QueryWrapper<>();
        query.eq("username", admin.getUsername());

        // 之前我们在数据库里直接插入的是 123456 明文，所以这里直接比对明文
        // 真实上线时管理员密码也必须像学生那样做 MD5 加密
        query.eq("password", admin.getPassword());

        SysAdmin dbAdmin = sysAdminService.getOne(query);

        if (dbAdmin == null) {
            return Result.error("管理员账号或密码错误！");
        }

        // 账号是否被禁用 (1:正常, 0:禁用)
        if (dbAdmin.getStatus() != null && dbAdmin.getStatus() == 0) {
            return Result.error("该账号已被禁用，请联系超级管理员！");
        }

        // 安全起见，剔除密码后返回
        dbAdmin.setPassword(null);
        return Result.success("管理员登录成功", dbAdmin);
    }
    @Autowired
    private SysEnterpriseMapper sysEnterpriseMapper;

    // 1. 获取所有待审核的企业列表 (不包含图片，减轻网络负担)
    @GetMapping("/enterprise/pending")
    public Result<?> getPendingEnterprises() {
        QueryWrapper<SysEnterprise> query = new QueryWrapper<>();
        query.eq("status", 0); // 0 表示待审核
        query.select("id", "account", "enterprise_name", "legal_person", "create_time");
        query.orderByDesc("create_time");
        return Result.success(sysEnterpriseMapper.selectList(query));
    }

    // 2. 获取企业详细信息 (包含营业执照图片的 Base64 编码)
    @GetMapping("/enterprise/detail/{id}")
    public Result<?> getEnterpriseDetail(@PathVariable Long id) {
        SysEnterprise ent = sysEnterpriseMapper.selectById(id);
        if (ent == null) return Result.error("未找到该企业");

        Map<String, Object> data = new HashMap<>();
        data.put("id", ent.getId());
        data.put("enterpriseName", ent.getEnterpriseName());
        data.put("legalPerson", ent.getLegalPerson());

        // 将数据库的 byte[] 转换为前端能直接 <img src="..."> 的 Base64 字符串
        if (ent.getLicenseImage() != null) {
            String base64Str = Base64.encode(ent.getLicenseImage());
            data.put("licenseImage", "data:image/jpeg;base64," + base64Str);
        }
        return Result.success(data);
    }

    // 3. 审核企业 (通过或驳回)
    @PostMapping("/enterprise/audit")
    public Result<?> auditEnterprise(@RequestBody SysEnterprise enterprise) {
        // 前端传过来 id 和 status (1通过, 2驳回)
        SysEnterprise updateEnt = new SysEnterprise();
        updateEnt.setId(enterprise.getId());
        updateEnt.setStatus(enterprise.getStatus());
        sysEnterpriseMapper.updateById(updateEnt);

        String msg = enterprise.getStatus() == 1 ? "已通过该企业入驻！" : "已驳回该企业申请！";
        return Result.success(msg, null);
    }
}