package com.woj.wojbackendjudgeservice.codesandbox;


import com.woj.model.judge.ExecuteCodeRequest;
import com.woj.model.judge.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest);
}
