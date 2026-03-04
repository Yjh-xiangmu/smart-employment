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

    // ================= 以下为新增的企业信息维护接口 =================

    // 3. 获取企业信息接口
    @GetMapping("/profile/{id}")
    public Result<?> getProfile(@PathVariable("id") Long id) {
        SysEnterprise enterprise = sysEnterpriseMapper.selectById(id);
        if (enterprise != null) {
            // 为了安全和网络传输速度，把密码和图片字节码置空
            enterprise.setPassword(null);
            enterprise.setLicenseImage(null);
            return Result.success("获取成功", enterprise);
        }
        return Result.error("企业信息不存在！");
    }

    // 4. 更新企业信息接口
    @PutMapping("/update")
    public Result<?> updateProfile(@RequestBody SysEnterprise enterprise) {
        if (enterprise.getId() == null) {
            return Result.error("企业ID不能为空！");
        }

        // 构造一个新的实体类进行更新，防止前端恶意篡改密码、状态等敏感字段
        SysEnterprise updateEntity = new SysEnterprise();
        updateEntity.setId(enterprise.getId());
        updateEntity.setEnterpriseName(enterprise.getEnterpriseName());
        updateEntity.setLegalPerson(enterprise.getLegalPerson()); // 法人也可修改

        // 注入新加的字段
        updateEntity.setAddress(enterprise.getAddress());
        updateEntity.setIndustry(enterprise.getIndustry());
        updateEntity.setScale(enterprise.getScale());
        updateEntity.setContactPerson(enterprise.getContactPerson());
        updateEntity.setContactPhone(enterprise.getContactPhone());
        updateEntity.setDescription(enterprise.getDescription());

        int rows = sysEnterpriseMapper.updateById(updateEntity);
        if (rows > 0) {
            return Result.success("企业信息更新成功！", null);
        }
        return Result.error("更新失败，请重试！");
    }
}