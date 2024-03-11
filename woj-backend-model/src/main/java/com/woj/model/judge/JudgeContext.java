package com.woj.model.judge;


import com.woj.model.entity.Question;
import com.woj.model.enums.QuestionSubmitLanguageEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JudgeContext {
    private ExecuteCodeResponse executeCodeResponse;
    private Question question;
    private QuestionSubmitLanguageEnum language;
}
