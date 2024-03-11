package com.woj.wojbackendjudgeservice.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.woj.model.dto.question.JudgeCase;
import com.woj.model.dto.question.JudgeConfig;
import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.entity.Question;
import com.woj.model.enums.JudgeInfoMessageEnum;
import com.woj.model.enums.QuestionSubmitStatusEnum;
import com.woj.model.judge.ExecuteCodeResponse;
import com.woj.model.judge.JudgeContext;
import com.woj.wojbackendjudgeservice.strategy.JudgeStrategy;


import java.util.List;

public class DefaultJudgeStrategy implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        ExecuteCodeResponse executeCodeResponse = judgeContext.getExecuteCodeResponse();
        Question question = judgeContext.getQuestion();

        List<String> outputList = executeCodeResponse.getOutputList();
        Integer status = executeCodeResponse.getStatus();
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();

        List<JudgeCase> judgeCaseList = JSONUtil.toList(question.getJudgeCase(), JudgeCase.class);
        JudgeConfig judgeConfig = new Gson().fromJson(question.getJudgeConfig(), JudgeConfig.class);

        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setTime(judgeInfo.getTime());
        // 原生代码沙箱没有检查内存
        if(judgeInfo.getMemory()!=null)
            judgeInfoResponse.setMemory(judgeInfo.getMemory());
        else
            judgeInfoResponse.setMemory(0L);
        if(!status.equals(QuestionSubmitStatusEnum.SUCCEED.getValue())){
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getText());
            return judgeInfoResponse;
        }
        if(outputList.size()!=judgeCaseList.size()){
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getText());
            return judgeInfoResponse;
        }
        for(int i=0;i<outputList.size();i++){
            String output1 = judgeCaseList.get(i).getOutput();
            String output2=outputList.get(i);
            if(!output1.equals(output2)){
                judgeInfoResponse.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
                return judgeInfoResponse;
            }
        }

        if(judgeConfig.getMemoryLimit()<judgeInfoResponse.getMemory()){
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getText());
            return judgeInfoResponse;
        }
        if(judgeConfig.getTimeLimit()<judgeInfoResponse.getTime()){
            judgeInfoResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return judgeInfoResponse;
        }
        judgeInfoResponse.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        return judgeInfoResponse;
    }
}
