package com.woj.wojbackendjudgeservice.codesandbox.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.woj.model.enums.QuestionSubmitStatusEnum;
import com.woj.model.judge.ExecuteCodeRequest;
import com.woj.model.judge.ExecuteCodeResponse;
import com.woj.wojbackendjudgeservice.codesandbox.CodeSandbox;

public class RemoteCodeSandbox implements CodeSandbox {

    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("remote");
        HttpResponse response = HttpUtil.createPost("http://localhost:8090/codesandbox/executeCode")
                .header("auth","secret")
                .body(JSONUtil.toJsonStr(executeCodeRequest))
                .execute(false);
        if(response.getStatus()!=200){
            System.out.println("http状态码:"+response.getStatus());
            ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
            executeCodeResponse.setMessage("http状态码:"+response.getStatus());
            executeCodeResponse.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            return executeCodeResponse;
        }
        ExecuteCodeResponse executeCodeResponse = JSONUtil.toBean(response.body(), ExecuteCodeResponse.class);
        return executeCodeResponse;
    }
}
