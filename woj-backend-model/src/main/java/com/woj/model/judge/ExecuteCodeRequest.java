package com.woj.model.judge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ExecuteCodeRequest implements Serializable {
    private List<String> inputList;
    private String language;
    private String code;
    private static final long serialVersionUID = 1L;
}
