package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysResumeDelivery;
import com.employment.backend.entity.SysStudentProfile;
import com.employment.backend.entity.SysJob;
import com.employment.backend.entity.SysMessage;
import com.employment.backend.mapper.SysResumeDeliveryMapper;
import com.employment.backend.mapper.SysStudentProfileMapper;
import com.employment.backend.mapper.SysJobMapper;
import com.employment.backend.mapper.SysMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student/delivery")
public class StudentDeliveryController {

    @Autowired
    private SysResumeDeliveryMapper deliveryMapper;

    @Autowired
    private SysStudentProfileMapper profileMapper;

    // 新增：引入职位和消息的 Mapper
    @Autowired
    private SysJobMapper sysJobMapper;

    @Autowired
    private SysMessageMapper sysMessageMapper;

    // 1. 投递简历接口
    @PostMapping("/apply")
    public Result<?> applyJob(@RequestBody SysResumeDelivery delivery) {
        // 1. 拦截校验：检查学生是否已经在个人中心上传了附件简历
        QueryWrapper<SysStudentProfile> profileQuery = new QueryWrapper<>();
        profileQuery.eq("student_id", delivery.getStudentId());
        SysStudentProfile profile = profileMapper.selectOne(profileQuery);

        if (profile == null || profile.getResumeName() == null || profile.getResumeName().isEmpty()) {
            return Result.error("投递失败：您还没有上传附件简历，请先到【个人中心】上传 PDF/Word 简历！");
        }

        // 2. 拦截校验：检查是否已经投递过该岗位，防止重复投递
        QueryWrapper<SysResumeDelivery> existQuery = new QueryWrapper<>();
        existQuery.eq("student_id", delivery.getStudentId())
                .eq("job_id", delivery.getJobId())
                .ne("status", 4); // 如果之前取消了(4)，允许再次投递

        if (deliveryMapper.selectCount(existQuery) > 0) {
            return Result.error("您已经投递过该岗位，请勿重复投递！");
        }

        // 3. 校验通过，生成投递记录
        delivery.setStatus(0); // 默认 0 待处理
        deliveryMapper.insert(delivery);

        // ================= 【核心新增】给企业发送系统消息 =================
        // 先查出职位信息（为了拿到企业ID和职位名称）
        SysJob job = sysJobMapper.selectById(delivery.getJobId());
        if (job != null) {
            SysMessage msgEntity = new SysMessage();
            msgEntity.setReceiverId(job.getEnterpriseId()); // 接收者是该职位所属的企业
            msgEntity.setReceiverType(2); // 2 代表接收者是企业
            msgEntity.setTitle("新简历投递提醒");
            msgEntity.setContent("系统提示：有一名学生刚刚投递了贵公司的【" + job.getJobName() + "】岗位，请前往简历库查看处理。");
            msgEntity.setIsRead(0); // 默认未读
            msgEntity.setCreateTime(LocalDateTime.now());
            sysMessageMapper.insert(msgEntity);
        }
        // =================================================================

        return Result.success("投递成功！企业HR将尽快处理您的简历。", null);
    }

    // 2. 获取学生的投递记录列表
    @GetMapping("/list/{studentId}")
    public Result<?> getDeliveryList(@PathVariable Long studentId) {
        List<Map<String, Object>> list = deliveryMapper.getStudentDeliveryList(studentId);
        return Result.success(list);
    }

    // 3. 取消投递 (仅限状态为 0-待处理 的记录)
    @PostMapping("/cancel/{deliveryId}")
    public Result<?> cancelDelivery(@PathVariable Long deliveryId) {
        SysResumeDelivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null) {
            return Result.error("投递记录不存在！");
        }

        if (delivery.getStatus() != 0) {
            return Result.error("该简历企业已在处理中，无法取消！");
        }

        // 将状态修改为 4:已取消
        delivery.setStatus(4);
        deliveryMapper.updateById(delivery);

        // ================= 【核心新增】给企业发送系统消息 =================
        SysJob job = sysJobMapper.selectById(delivery.getJobId());
        if (job != null) {
            SysMessage msgEntity = new SysMessage();
            msgEntity.setReceiverId(job.getEnterpriseId());
            msgEntity.setReceiverType(2); // 2 代表接收者是企业
            msgEntity.setTitle("学生取消投递通知");
            msgEntity.setContent("系统提示：有一名学生取消了对贵公司【" + job.getJobName() + "】岗位的投递申请。");
            msgEntity.setIsRead(0);
            msgEntity.setCreateTime(LocalDateTime.now());
            sysMessageMapper.insert(msgEntity);
        }
        // =================================================================

        return Result.success("已成功取消投递！", null);
    }
}