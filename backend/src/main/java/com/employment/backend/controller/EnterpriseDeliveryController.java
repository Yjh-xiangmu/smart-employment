package com.employment.backend.controller;

import com.employment.backend.common.Result;
import com.employment.backend.entity.SysEnterprise;
import com.employment.backend.entity.SysJob;
import com.employment.backend.entity.SysMessage;
import com.employment.backend.entity.SysResumeDelivery;
import com.employment.backend.mapper.SysEnterpriseMapper;
import com.employment.backend.mapper.SysJobMapper;
import com.employment.backend.mapper.SysMessageMapper;
import com.employment.backend.mapper.SysResumeDeliveryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enterprise/delivery")
public class EnterpriseDeliveryController {

    @Autowired
    private SysResumeDeliveryMapper deliveryMapper;
    @Autowired
    private SysMessageMapper messageMapper;
    @Autowired
    private SysJobMapper jobMapper;
    @Autowired
    private SysEnterpriseMapper enterpriseMapper;

    @GetMapping("/list/{enterpriseId}")
    public Result<?> getList(@PathVariable Long enterpriseId) {
        List<Map<String, Object>> list = deliveryMapper.getEnterpriseDeliveryList(enterpriseId);
        return Result.success(list);
    }

    @PostMapping("/changeStatus")
    public Result<?> changeStatus(@RequestBody SysResumeDelivery delivery) {
        // 1. 更新投递状态
        SysResumeDelivery updateRecord = new SysResumeDelivery();
        updateRecord.setId(delivery.getId());
        updateRecord.setStatus(delivery.getStatus());
        deliveryMapper.updateById(updateRecord);

        // 2. 如果是邀约(2)或淘汰(3)，自动给学生发送站内信
        if (delivery.getStatus() == 2 || delivery.getStatus() == 3) {
            // 先查出原投递记录拿到 studentId, jobId 和 enterpriseId
            SysResumeDelivery origin = deliveryMapper.selectById(delivery.getId());
            SysJob job = jobMapper.selectById(origin.getJobId());
            SysEnterprise ent = enterpriseMapper.selectById(origin.getEnterpriseId());

            SysMessage msg = new SysMessage();
            msg.setReceiverId(origin.getStudentId());
            msg.setIsRead(0);

            if (delivery.getStatus() == 2) {
                msg.setTitle("面试邀约通知");
                msg.setContent("恭喜！【" + ent.getEnterpriseName() + "】对您的简历很感兴趣，邀请您参加【" + job.getJobName() + "】岗位的面试。请保持电话畅通，HR将很快与您联系！");
            } else {
                msg.setTitle("简历反馈通知");
                msg.setContent("很遗憾，您投递给【" + ent.getEnterpriseName() + "】的【" + job.getJobName() + "】岗位，经过评估暂时不太匹配。不要灰心，继续寻找更适合您的机会吧！");
            }
            messageMapper.insert(msg);
        }

        return Result.success("操作成功！已自动通知该求职者。", null);
    }
}