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
@RequestMapping("/student/message")
public class MessageController {

    @Autowired
    private SysMessageMapper messageMapper;

    // 1. 获取消息列表和未读数量
    @GetMapping("/list/{studentId}")
    public Result<?> getMessageList(@PathVariable Long studentId) {
        QueryWrapper<SysMessage> query = new QueryWrapper<>();
        query.eq("receiver_id", studentId).orderByDesc("create_time");
        List<SysMessage> list = messageMapper.selectList(query);

        // 统计未读数量
        long unreadCount = list.stream().filter(m -> m.getIsRead() == 0).count();

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("unreadCount", unreadCount);
        return Result.success(data);
    }

    // 2. 将某条消息标记为已读
    @PostMapping("/read/{messageId}")
    public Result<?> readMessage(@PathVariable Long messageId) {
        SysMessage msg = new SysMessage();
        msg.setId(messageId);
        msg.setIsRead(1);
        messageMapper.updateById(msg);
        // 修改这里：传入 null 或者提示语
        return Result.success("已读成功", null);
    }

    // 3. 一键已读
    @PostMapping("/readAll/{studentId}")
    public Result<?> readAll(@PathVariable Long studentId) {
        SysMessage msg = new SysMessage();
        msg.setIsRead(1);
        QueryWrapper<SysMessage> query = new QueryWrapper<>();
        query.eq("receiver_id", studentId).eq("is_read", 0);
        messageMapper.update(msg, query);
        // 修改这里：传入 null 或者提示语
        return Result.success("全部已读成功", null);
    }
}