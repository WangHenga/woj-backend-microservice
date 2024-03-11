package com.woj.wojbackendserviceclient.service;

import com.woj.model.entity.Question;
import com.woj.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
* @author WangH
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-12-15 16:28:12
*/
@FeignClient(name="woj-backend-question-service",path = "/api/question/inner")
public interface QuestionFeignClient {
    @GetMapping("/get")
    Question getById(@RequestParam("id") Long id);
    @PostMapping("/update")
    boolean updateById(@RequestBody Question question);
    @GetMapping("/submit/get")
    QuestionSubmit getSubmitById(@RequestParam("id") Long id);
    @PostMapping("/submit/update")
    boolean updateSubmitById(@RequestBody QuestionSubmit questionSubmit);
}
