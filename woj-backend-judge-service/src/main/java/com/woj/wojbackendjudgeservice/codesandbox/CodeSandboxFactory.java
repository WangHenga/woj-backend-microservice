package com.woj.wojbackendjudgeservice.codesandbox;


import com.woj.wojbackendjudgeservice.codesandbox.impl.ExampleCodeSandbox;
import com.woj.wojbackendjudgeservice.codesandbox.impl.RemoteCodeSandbox;

public class CodeSandboxFactory {
    public static CodeSandbox newInstance(String type){
        switch (type){
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
