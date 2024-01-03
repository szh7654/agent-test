package com.example.demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

// 或者
@RestController
public class TestController{

    // 添加GET一个接口，返回helloworld字符串
    @GetMapping("test")
    public String test(HttpServletResponse response){
        response.addCookie(new Cookie("name", "value"));
        return "helloworld";
    }
}
