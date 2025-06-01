package com.koko.mywiki.service;

import com.koko.mywiki.domain.Test;
import com.koko.mywiki.mapper.TestMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestService {

    @Resource
    private TestMapper testMapper;

    public List<Test> list() {
        return testMapper.list();
    }
}
