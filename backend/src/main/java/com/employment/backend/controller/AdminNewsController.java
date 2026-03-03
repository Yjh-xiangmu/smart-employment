package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysNews;
import com.employment.backend.mapper.SysNewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/news")
public class AdminNewsController {

    @Autowired
    private SysNewsMapper sysNewsMapper;

    // 1. 获取新闻列表 (支持按标题搜索和类型筛选)
    @GetMapping("/list")
    public Result<?> getList(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer type) {
        QueryWrapper<SysNews> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("title", keyword);
        }
        if (type != null) {
            query.eq("type", type);
        }
        query.orderByDesc("create_time");

        return Result.success(sysNewsMapper.selectList(query));
    }

    // 2. 发布或更新新闻
    @PostMapping("/save")
    public Result<?> saveNews(@RequestBody SysNews news) {
        if (news.getId() == null) {
            news.setViewCount(0);
            sysNewsMapper.insert(news);
            return Result.success("发布成功！", null);
        } else {
            sysNewsMapper.updateById(news);
            return Result.success("修改成功！", null);
        }
    }

    // 3. 删除新闻
    @PostMapping("/delete/{id}")
    public Result<?> deleteNews(@PathVariable Long id) {
        sysNewsMapper.deleteById(id);
        return Result.success("删除成功！", null);
    }
}