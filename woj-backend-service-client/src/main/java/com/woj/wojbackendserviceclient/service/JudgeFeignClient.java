package com.woj.wojbackendserviceclient.service;


import com.woj.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "woj-backend-judge-service",path = "/api/judge/inner")
public interface JudgeFeignClient {
    @GetMapping("/do")
    QuestionSubmit doJudge(@RequestParam("id") Long id);
}
