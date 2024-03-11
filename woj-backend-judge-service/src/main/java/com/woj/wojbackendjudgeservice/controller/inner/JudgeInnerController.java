package com.woj.wojbackendjudgeservice.controller.inner;

import com.woj.model.entity.QuestionSubmit;
import com.woj.wojbackendjudgeservice.JudgeService;
import com.woj.wojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {
    @Resource
    private JudgeService judgeService;
    @Override
    @GetMapping("/do")
    public QuestionSubmit doJudge(@RequestParam("id") Long id) {
        return judgeService.doJudge(id);
    }
}
