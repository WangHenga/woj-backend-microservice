package com.woj.wojbackendjudgeservice.strategy;


import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.judge.JudgeContext;

public interface JudgeStrategy {
     JudgeInfo doJudge(JudgeContext judgeContext);
}
