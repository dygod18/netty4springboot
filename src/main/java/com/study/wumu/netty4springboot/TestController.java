package com.study.wumu.netty4springboot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by dydy on 2018/3/8.
 */
@RestController
public class TestController {

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        System.out.println("success：" + System.currentTimeMillis());
        return "success";
    }

}
