package com.koko.mywiki.controller;

import com.koko.mywiki.domain.Demo;
import com.koko.mywiki.service.DemoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DemoController {


    @Resource
    private DemoService demoService;
    

    @GetMapping("/demo/list")
    public List<Demo> list() {
        return demoService.list();
    }
}
