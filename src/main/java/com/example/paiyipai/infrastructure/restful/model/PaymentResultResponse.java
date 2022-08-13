package com.example.paiyipai.infrastructure.restful.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class PaymentResultResponse {

    private String pid;

    private String result;
}
