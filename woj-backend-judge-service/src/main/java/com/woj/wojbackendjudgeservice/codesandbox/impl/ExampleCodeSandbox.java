package com.woj.wojbackendjudgeservice.codesandbox.impl;



import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.enums.JudgeInfoMessageEnum;
import com.woj.model.enums.QuestionSubmitStatusEnum;
import com.woj.model.judge.ExecuteCodeRequest;
import com.woj.model.judge.ExecuteCodeResponse;
import com.woj.wojbackendjudgeservice.codesandbox.CodeSandbox;

import java.util.List;

public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        executeCodeResponse.setMessage("测试执行成功");
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
