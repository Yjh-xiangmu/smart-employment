package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysMessage;
import com.employment.backend.mapper.SysMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enterprise/message")
public class EnterpriseMessageController {

    @Autowired
    private SysMessageMapper messageMapper;

    @GetMapping("/list/{enterpriseId}")
    public Result<?> getMessageList(@PathVariable Long enterpriseId) {
        QueryWrapper<SysMessage> query = new QueryWrapper<>();
        // 限定查询企业的消息(type=2)
        query.eq("receiver_id", enterpriseId).eq("receiver_type", 2).orderByDesc("create_time");
        List<SysMessage> list = messageMapper.selectList(query);

        long unreadCount = list.stream().filter(m -> m.getIsRead() == 0).count();

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("unreadCount", unreadCount);
        return Result.success(data);
    }

    @PostMapping("/read/{messageId}")
    public Result<?> readMessage(@PathVariable Long messageId) {
        SysMessage msg = new SysMessage();
        msg.setId(messageId);
        msg.setIsRead(1);
        messageMapper.updateById(msg);
        return Result.success("已读成功", null);
    }

    @PostMapping("/readAll/{enterpriseId}")
    public Result<?> readAll(@PathVariable Long enterpriseId) {
        SysMessage msg = new SysMessage();
        msg.setIsRead(1);
        QueryWrapper<SysMessage> query = new QueryWrapper<>();
        query.eq("receiver_id", enterpriseId).eq("receiver_type", 2).eq("is_read", 0);
        messageMapper.update(msg, query);
        return Result.success("全部已读成功", null);
    }
}