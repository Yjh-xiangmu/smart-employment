package com.employment.backend.controller;

import com.employment.backend.common.Result;
import com.employment.backend.entity.SysForumPost;
import com.employment.backend.mapper.SysForumPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.employment.backend.entity.SysForumComment;
import com.employment.backend.mapper.SysForumCommentMapper;
@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private SysForumPostMapper forumPostMapper;

    // 1. 获取帖子列表 (包含搜索)
    @GetMapping("/list")
    public Result<?> getList(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = forumPostMapper.getForumList(keyword);
        return Result.success(list);
    }

    // 2. 学生发布新帖子
    @PostMapping("/add")
    public Result<?> addPost(@RequestBody SysForumPost post) {
        post.setViewCount(0); // 初始浏览量为0
        forumPostMapper.insert(post);
        return Result.success("发布成功！", null);
    }

    // 3. 增加浏览量 (每次点开帖子时调用)
    @PostMapping("/view/{id}")
    public Result<?> addViewCount(@PathVariable Long id) {
        SysForumPost post = forumPostMapper.selectById(id);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            forumPostMapper.updateById(post);
        }
        return Result.success("浏览量+1", null);
    }
    @Autowired
    private SysForumCommentMapper forumCommentMapper;

    // 4. 获取某篇帖子的所有评论
    @GetMapping("/comment/list/{postId}")
    public Result<?> getCommentList(@PathVariable Long postId) {
        List<Map<String, Object>> list = forumCommentMapper.getCommentsByPostId(postId);
        return Result.success(list);
    }

    // 5. 发表评论
    @PostMapping("/comment/add")
    public Result<?> addComment(@RequestBody SysForumComment comment) {
        forumCommentMapper.insert(comment);
        return Result.success("评论发表成功！", null);
    }
}