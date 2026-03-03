package com.employment.backend.controller;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysStudent;
import com.employment.backend.mapper.SysStudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import java.net.URLEncoder;
@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private SysStudentMapper sysStudentMapper;

    // 1. 注册接口
    @PostMapping("/register")
    public Result<?> register(@RequestBody SysStudent student) {
        // 检查学号是否已存在
        QueryWrapper<SysStudent> query = new QueryWrapper<>();
        query.eq("student_no", student.getStudentNo());
        if (sysStudentMapper.selectCount(query) > 0) {
            return Result.error("学号已存在，请直接登录！");
        }

        // 密码 MD5 加密存储 (真实的系统绝不能存明文密码)
        student.setPassword(SecureUtil.md5(student.getPassword()));
        sysStudentMapper.insert(student);
        return Result.success("注册成功！", null);
    }

    // 2. 登录接口
    @PostMapping("/login")
    public Result<?> login(@RequestBody SysStudent student) {
        QueryWrapper<SysStudent> query = new QueryWrapper<>();
        query.eq("student_no", student.getStudentNo());
        // 将前端传来的密码同样进行MD5加密后，再去数据库比对
        query.eq("password", SecureUtil.md5(student.getPassword()));

        SysStudent dbStudent = sysStudentMapper.selectOne(query);
        if (dbStudent == null) {
            return Result.error("学号或密码错误！");
        }

        // 登录成功，清除密码后返回给前端 (安全起见)
        dbStudent.setPassword(null);
        return Result.success("登录成功", dbStudent);
    }
    // 3. 找回密码/重置密码接口
    @PostMapping("/forgotPassword")
    public Result<?> forgotPassword(@RequestBody SysStudent student) {
        // 校验学号和姓名是否匹配
        QueryWrapper<SysStudent> query = new QueryWrapper<>();
        query.eq("student_no", student.getStudentNo());
        query.eq("name", student.getName()); // 必须输入当初注册时的真实姓名

        SysStudent dbStudent = sysStudentMapper.selectOne(query);
        if (dbStudent == null) {
            return Result.error("学号或姓名不匹配，无法找回密码！");
        }

        // 匹配成功，将前端传来的新密码加密并保存
        dbStudent.setPassword(cn.hutool.crypto.SecureUtil.md5(student.getPassword()));
        sysStudentMapper.updateById(dbStudent);

        return Result.success("密码重置成功，请使用新密码登录！", null);
    }
    @Autowired
    private com.employment.backend.mapper.SysStudentProfileMapper profileMapper;

    // 4. 获取个人档案
    @GetMapping("/profile/{studentId}")
    public Result<?> getProfile(@PathVariable Long studentId) {
        QueryWrapper<com.employment.backend.entity.SysStudentProfile> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        com.employment.backend.entity.SysStudentProfile profile = profileMapper.selectOne(query);

        if (profile != null) {
            profile.setResumeFile(null); // 列表查询时不返回二进制文件，节省带宽
        }
        return Result.success(profile);
    }

    // 5. 保存或更新个人档案 (基本信息 + 求职意向)
    @PostMapping("/profile/save")
    public Result<?> saveProfile(@RequestBody com.employment.backend.entity.SysStudentProfile profile) {
        QueryWrapper<com.employment.backend.entity.SysStudentProfile> query = new QueryWrapper<>();
        query.eq("student_id", profile.getStudentId());
        com.employment.backend.entity.SysStudentProfile exist = profileMapper.selectOne(query);

        if (exist == null) {
            profileMapper.insert(profile);
        } else {
            profile.setId(exist.getId());
            profileMapper.updateById(profile);
        }
        return Result.success("保存成功", null);
    }

    // 6. 上传简历附件 (PDF/Word)
    @PostMapping("/profile/uploadResume")
    public Result<?> uploadResume(@RequestParam("studentId") Long studentId,
                                  @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file.isEmpty()) return Result.error("文件不能为空");

            QueryWrapper<com.employment.backend.entity.SysStudentProfile> query = new QueryWrapper<>();
            query.eq("student_id", studentId);
            com.employment.backend.entity.SysStudentProfile profile = profileMapper.selectOne(query);

            if (profile == null) {
                profile = new com.employment.backend.entity.SysStudentProfile();
                profile.setStudentId(studentId);
                profile.setResumeFile(file.getBytes());
                profile.setResumeName(file.getOriginalFilename());
                profileMapper.insert(profile);
            } else {
                profile.setResumeFile(file.getBytes());
                profile.setResumeName(file.getOriginalFilename());
                profileMapper.updateById(profile);
            }
            return Result.success("简历上传成功", profile.getResumeName());
        } catch (Exception e) {
            return Result.error("简历上传失败");
        }
    }
    // 7. 预览/下载简历附件
    @GetMapping("/profile/downloadResume/{studentId}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long studentId) {
        QueryWrapper<com.employment.backend.entity.SysStudentProfile> query = new QueryWrapper<>();
        query.eq("student_id", studentId);
        com.employment.backend.entity.SysStudentProfile profile = profileMapper.selectOne(query);

        // 如果找不到档案或者没有上传过文件，返回 404
        if (profile == null || profile.getResumeFile() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 处理中文文件名乱码问题
            String fileName = URLEncoder.encode(profile.getResumeName(), "UTF-8").replaceAll("\\+", "%20");

            HttpHeaders headers = new HttpHeaders();
            // inline 表示如果浏览器支持（如PDF），则直接在网页内预览；如果不支持（如Word），则自动下载
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename*=utf-8''" + fileName);

            // 根据后缀名设置 Content-Type
            if (profile.getResumeName().toLowerCase().endsWith(".pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            return new ResponseEntity<>(profile.getResumeFile(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}