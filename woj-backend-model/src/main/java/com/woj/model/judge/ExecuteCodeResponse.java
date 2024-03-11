package com.woj.model.judge;

import com.woj.model.dto.questionsubmit.JudgeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExecuteCodeResponse implements Serializable {
    private List<String> outputList;
    private Integer status;
    private String message;
    private JudgeInfo judgeInfo;
    private static final long serialVersionUID = 1L;
}
