package com.employment.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.employment.backend.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {
    // 继承了 BaseMapper，MyBatis-Plus 已经自动帮我们写好了增删改查的方法，这里无需写代码
}