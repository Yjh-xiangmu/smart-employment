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

import java.time.LocalDateTime;
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

        // 2. 如果是邀约(2)、淘汰(3) 或 录用(5)，自动给学生发送系统消息
        if (delivery.getStatus() == 2 || delivery.getStatus() == 3 || delivery.getStatus() == 5) {
            SysResumeDelivery origin = deliveryMapper.selectById(delivery.getId());
            SysJob job = jobMapper.selectById(origin.getJobId());
            SysEnterprise ent = enterpriseMapper.selectById(origin.getEnterpriseId());

            SysMessage msg = new SysMessage();
            msg.setReceiverId(origin.getStudentId());
            msg.setReceiverType(1); // 1 代表发送给学生
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now()); // 设置消息发送时间

            if (delivery.getStatus() == 2) {
                msg.setTitle("面试邀约通知");
                msg.setContent("恭喜！【" + ent.getEnterpriseName() + "】对您的简历很感兴趣，邀请您参加【" + job.getJobName() + "】岗位的面试。请保持电话畅通，HR将很快与您联系！");
            } else if (delivery.getStatus() == 3) {
                msg.setTitle("简历反馈通知");
                msg.setContent("很遗憾，您投递给【" + ent.getEnterpriseName() + "】的【" + job.getJobName() + "】岗位，经过评估或面试，暂时不太匹配。不要灰心，继续寻找更适合您的机会吧！");
            } else if (delivery.getStatus() == 5) {
                msg.setTitle("录用/Offer通知");
                msg.setContent("热烈祝贺！您在【" + ent.getEnterpriseName() + "】的【" + job.getJobName() + "】面试中表现优异，已被正式录用！HR将很快与您沟通具体的入职事宜。");
            }
            messageMapper.insert(msg);
        }

        return Result.success("操作成功！已自动通知该求职者。", null);
    }
}