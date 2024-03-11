package com.woj.wojbackendquestionservice.controller.inner;

import com.woj.model.entity.Question;
import com.woj.model.entity.QuestionSubmit;
import com.woj.wojbackendquestionservice.service.QuestionService;
import com.woj.wojbackendquestionservice.service.QuestionSubmitService;
import com.woj.wojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {
    @Resource
    private QuestionService questionService;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Override
    @GetMapping("/get")
    public Question getById(@RequestParam("id") Long id){
        return questionService.getById(id);
    }
    @Override
    @PostMapping("/update")
    public boolean updateById(@RequestBody Question question){
        return questionService.updateById(question);
    }
    @Override
    @GetMapping("/submit/get")
    public QuestionSubmit getSubmitById(@RequestParam("id") Long id){
        return questionSubmitService.getById(id);
    }
    @Override
    @PostMapping("/submit/update")
    public boolean updateSubmitById(@RequestBody QuestionSubmit questionSubmit){
        return questionSubmitService.updateById(questionSubmit);
    }
}
