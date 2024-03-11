package com.woj.wojbackendjudgeservice.strategy;


import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.judge.JudgeContext;
import com.woj.wojbackendjudgeservice.strategy.impl.DefaultJudgeStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JudgeManager {
    public static JudgeInfo doJudge(JudgeContext judgeContext){
        log.info("language:{}",judgeContext.getLanguage().getText());
        JudgeStrategy judgeStrategy=new DefaultJudgeStrategy();
        return judgeStrategy.doJudge(judgeContext);
    }
}
