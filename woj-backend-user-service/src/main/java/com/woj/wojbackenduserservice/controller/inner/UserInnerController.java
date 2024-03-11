package com.woj.wojbackenduserservice.controller.inner;

import com.woj.model.entity.User;
import com.woj.wojbackendserviceclient.service.UserFeignClient;
import com.woj.wojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeignClient {
    @Resource
    private UserService userService;
    @Override
    @GetMapping("/get")
    public User getById(@RequestParam("id") Long id) {
        return userService.getById(id);
    }
}
