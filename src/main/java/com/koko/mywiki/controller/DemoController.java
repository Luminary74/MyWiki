package com.koko.mywiki.controller;

import com.koko.mywiki.domain.Demo;
import com.koko.mywiki.service.DemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {
    

    @Resource
    private DemoService demoService;


    @GetMapping("/list")
    public List<Demo> list() {
        return demoService.list();
    }
}
