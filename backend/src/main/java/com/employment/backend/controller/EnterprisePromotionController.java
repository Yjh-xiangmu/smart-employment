package com.employment.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.employment.backend.common.Result;
import com.employment.backend.entity.SysEnterprisePromotion;
import com.employment.backend.mapper.SysEnterprisePromotionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/enterprise/promotion")
public class EnterprisePromotionController {

    @Autowired
    private SysEnterprisePromotionMapper promotionMapper;

    // 1. 获取当前企业的宣传列表
    @GetMapping("/list")
    public Result<?> getList(@RequestParam("enterpriseId") Long enterpriseId) {
        QueryWrapper<SysEnterprisePromotion> query = new QueryWrapper<>();
        query.eq("enterprise_id", enterpriseId).orderByDesc("create_time");
        return Result.success("获取成功", promotionMapper.selectList(query));
    }

    // 2. 企业发布新宣传
    @PostMapping("/add")
    public Result<?> addPromotion(@RequestBody SysEnterprisePromotion promotion) {
        if (promotion.getEnterpriseId() == null) {
            return Result.error("缺少企业ID信息！");
        }
        promotion.setViewCount(0); // 初始浏览量0
        promotion.setCreateTime(LocalDateTime.now());

        int rows = promotionMapper.insert(promotion);
        return rows > 0 ? Result.success("发布成功", null) : Result.error("发布失败");
    }

    // 3. 企业修改宣传内容
    @PutMapping("/update")
    public Result<?> updatePromotion(@RequestBody SysEnterprisePromotion promotion) {
        int rows = promotionMapper.updateById(promotion);
        return rows > 0 ? Result.success("修改成功", null) : Result.error("修改失败");
    }

    // 4. 企业删除宣传
    @DeleteMapping("/delete/{id}")
    public Result<?> deletePromotion(@PathVariable("id") Long id) {
        int rows = promotionMapper.deleteById(id);
        return rows > 0 ? Result.success("删除成功", null) : Result.error("删除失败");
    }
}