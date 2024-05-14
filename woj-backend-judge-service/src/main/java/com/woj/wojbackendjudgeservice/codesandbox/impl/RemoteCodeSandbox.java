package com.woj.wojbackendjudgeservice.codesandbox.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.woj.model.enums.QuestionSubmitStatusEnum;
import com.woj.model.judge.ExecuteCodeRequest;
import com.woj.model.judge.ExecuteCodeResponse;
import com.woj.wojbackendjudgeservice.codesandbox.CodeSandbox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class RemoteCodeSandbox implements CodeSandbox {
    private static final String app_key="first";
    private static final String app_secret="wangheng";

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    @Override
    public ExecuteCodeResponse doExecute(ExecuteCodeRequest executeCodeRequest) {
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        SecretKeySpec secretKeySpec = new SecretKeySpec(app_secret.getBytes(StandardCharsets.UTF_8),HMAC_SHA256_ALGORITHM);
        Mac mac = null;
        String sign=null;
        long timestamp=System.currentTimeMillis()/1000;
        String nonce= UUID.randomUUID().toString(); // TODO 生成随机字符串
        try {
            mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacData = mac.doFinal((json+timestamp+nonce).getBytes(StandardCharsets.UTF_8));
            sign= Base64.getEncoder().encodeToString(hmacData);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        System.out.println("remote");
        HttpResponse response = HttpUtil.createPost("http://localhost:8090/codesandbox/executeCode")
                .header("appKey",app_key)
                .header("sign",sign)
                .header("timestamp", String.valueOf(timestamp))
                .header("nonce",nonce)
                .body(json)
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
