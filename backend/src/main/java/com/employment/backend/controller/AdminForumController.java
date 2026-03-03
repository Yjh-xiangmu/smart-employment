package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysForumComment;
import com.employment.backend.mapper.SysForumCommentMapper;
import com.employment.backend.mapper.SysForumPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/forum")
public class AdminForumController {

    @Autowired
    private SysForumPostMapper forumPostMapper;

    @Autowired
    private SysForumCommentMapper forumCommentMapper;

    // 1. 获取全站帖子列表 (复用之前写好的多表联查SQL)
    @GetMapping("/list")
    public Result<?> getList(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = forumPostMapper.getForumList(keyword);
        return Result.success(list);
    }

    // 2. 管理员强制删除帖子及所属评论
    @PostMapping("/delete/{postId}")
    public Result<?> deletePost(@PathVariable Long postId) {
        // 先删除该帖子下的所有评论
        QueryWrapper<SysForumComment> commentQuery = new QueryWrapper<>();
        commentQuery.eq("post_id", postId);
        forumCommentMapper.delete(commentQuery);

        // 再删除帖子本身
        forumPostMapper.deleteById(postId);

        return Result.success("该违规帖子及相关评论已彻底清除！", null);
    }
}