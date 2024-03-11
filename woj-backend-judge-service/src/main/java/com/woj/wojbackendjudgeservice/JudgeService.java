package com.woj.wojbackendjudgeservice;


import com.woj.model.entity.QuestionSubmit;

public interface JudgeService {
    QuestionSubmit doJudge(Long questionSubmitId);
}
