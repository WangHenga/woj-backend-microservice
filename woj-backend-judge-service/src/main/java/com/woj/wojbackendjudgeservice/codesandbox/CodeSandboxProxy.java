package com.woj.wojbackendjudgeservice.codesandbox;


import com.woj.model.judge.ExecuteCodeRequest;
import com.woj.model.judge.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class CodeSandboxProxy implements CodeSandbox{
    private CodeSandbox codeSandbox;
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest){
        log.info("ExecuteCodeRequest:{}",executeCodeRequest);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.doExecute(executeCodeRequest);
        log.info("ExecuteCodeResponse:{}",executeCodeResponse);
        return executeCodeResponse;
    }
}
