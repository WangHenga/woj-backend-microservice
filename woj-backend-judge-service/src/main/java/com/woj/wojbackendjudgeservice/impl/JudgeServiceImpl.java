package com.woj.wojbackendjudgeservice.impl;

import cn.hutool.json.JSONUtil;

import com.woj.common.common.ErrorCode;
import com.woj.common.exception.BusinessException;
import com.woj.model.dto.question.JudgeCase;
import com.woj.model.dto.questionsubmit.JudgeInfo;
import com.woj.model.entity.Question;
import com.woj.model.entity.QuestionSubmit;
import com.woj.model.enums.JudgeInfoMessageEnum;
import com.woj.model.enums.QuestionSubmitLanguageEnum;
import com.woj.model.enums.QuestionSubmitStatusEnum;
import com.woj.model.judge.ExecuteCodeRequest;
import com.woj.model.judge.ExecuteCodeResponse;
import com.woj.model.judge.JudgeContext;
import com.woj.wojbackendjudgeservice.JudgeService;
import com.woj.wojbackendjudgeservice.codesandbox.CodeSandboxFactory;
import com.woj.wojbackendjudgeservice.codesandbox.CodeSandboxProxy;
import com.woj.wojbackendjudgeservice.strategy.JudgeManager;
import com.woj.wojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private QuestionFeignClient questionFeignClient;

    @Value("${codeSandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(Long questionSubmitId) {
        QuestionSubmit questionSubmit = questionFeignClient.getSubmitById(questionSubmitId);
        if(questionSubmit==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getById(questionId);
        if(question==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目信息不存在");
        }
        if(!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"正在判题...");
        }
        QuestionSubmit questionSubmitUpdate=new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.PROCESSING.getValue());
        boolean update = questionFeignClient.updateSubmitById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目提交状态更新错误");
        }
        CodeSandboxProxy codeSandboxProxy = new CodeSandboxProxy(CodeSandboxFactory.newInstance(type));
        List<JudgeCase> judgeCases = JSONUtil.toList(question.getJudgeCase(), JudgeCase.class);
        List<String> inputList = judgeCases.stream().map(judgeCase -> judgeCase.getInput()).collect(Collectors.toList());

        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(questionSubmit.getCode())
                .language(questionSubmit.getLanguage())
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandboxProxy.doExecute(executeCodeRequest);
        JudgeContext judgeContext = JudgeContext.builder()
                .language(QuestionSubmitLanguageEnum.getEnumByValue(questionSubmit.getLanguage()))
                .question(question)
                .executeCodeResponse(executeCodeResponse)
                .build();
        if(!QuestionSubmitStatusEnum.SUCCEED.getValue().equals(executeCodeResponse.getStatus())){
            questionSubmitUpdate.setStatus(executeCodeResponse.getStatus());
        }else{
            JudgeInfo judgeInfo = JudgeManager.doJudge(judgeContext);
            if(JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())){
                // 题目通过数加1
                Question newQuestion = new Question();
                newQuestion.setId(question.getId());
                newQuestion.setAcceptedNum(question.getAcceptedNum()+1);
                questionFeignClient.updateById(newQuestion);
            }
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        }
        update = questionFeignClient.updateSubmitById(questionSubmitUpdate);
        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"题目提交状态更新错误");
        }
        return questionFeignClient.getSubmitById(questionSubmitId);
    }
}
