package com.employment.backend.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysEnterprise;
import com.employment.backend.mapper.SysEnterpriseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {

    @Autowired
    private SysEnterpriseMapper sysEnterpriseMapper;

    // 1. 企业注册接口 (注意这里用的是表单提交，而不是 JSON @RequestBody)
    @PostMapping("/register")
    public Result<?> register(
            @RequestParam("account") String account,
            @RequestParam("password") String password,
            @RequestParam("enterpriseName") String enterpriseName,
            @RequestParam("legalPerson") String legalPerson,
            @RequestParam("licenseImage") MultipartFile file) {

        try {
            // 检查账号是否已存在
            QueryWrapper<SysEnterprise> query = new QueryWrapper<>();
            query.eq("account", account);
            if (sysEnterpriseMapper.selectCount(query) > 0) {
                return Result.error("该账号已存在，请直接登录或修改账号！");
            }

            SysEnterprise enterprise = new SysEnterprise();
            enterprise.setAccount(account);
            // 密码加密
            enterprise.setPassword(SecureUtil.md5(password));
            enterprise.setEnterpriseName(enterpriseName);
            enterprise.setLegalPerson(legalPerson);
            enterprise.setStatus(0); // 默认 0 待审核

            // 将上传的文件转换为 byte[] 存入数据库
            if (file != null && !file.isEmpty()) {
                enterprise.setLicenseImage(file.getBytes());
            } else {
                return Result.error("请上传营业执照！");
            }

            sysEnterpriseMapper.insert(enterprise);
            return Result.success("企业注册申请已提交，请等待管理员审核！", null);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件处理失败或系统异常！");
        }
    }

    // 2. 企业登录接口
    @PostMapping("/login")
    public Result<?> login(@RequestBody SysEnterprise enterprise) {
        QueryWrapper<SysEnterprise> query = new QueryWrapper<>();
        query.eq("account", enterprise.getAccount());
        query.eq("password", SecureUtil.md5(enterprise.getPassword()));

        SysEnterprise dbEnterprise = sysEnterpriseMapper.selectOne(query);
        if (dbEnterprise == null) {
            return Result.error("账号或密码错误！");
        }

        // --- 核心业务逻辑：审核状态拦截 ---
        if (dbEnterprise.getStatus() == 0) {
            return Result.error("您的企业账号正在审核中，请耐心等待！");
        } else if (dbEnterprise.getStatus() == 2) {
            return Result.error("您的企业资质审核被驳回，请联系平台！");
        }

        // 登录成功，清除密码和图片数据(防止前端卡顿)后返回
        dbEnterprise.setPassword(null);
        dbEnterprise.setLicenseImage(null);
        return Result.success("登录成功", dbEnterprise);
    }
}