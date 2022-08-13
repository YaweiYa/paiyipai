package com.example.paiyipai.controller;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConfirmDepositRequest {

    private String pid;

    private String result;
}
