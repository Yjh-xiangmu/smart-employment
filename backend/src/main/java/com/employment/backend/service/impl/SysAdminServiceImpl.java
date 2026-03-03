package com.employment.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.employment.backend.entity.SysAdmin;
import com.employment.backend.mapper.SysAdminMapper;
import com.employment.backend.service.ISysAdminService;
import org.springframework.stereotype.Service;

@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements ISysAdminService {
}