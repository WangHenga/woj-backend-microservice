package com.woj.model.dto.question;

import lombok.Data;

import java.io.Serializable;

@Data
public class JudgeCase implements Serializable {
    private String input;
    private String output;
    private static final long serialVersionUID = 1L;
}
