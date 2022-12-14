package com.example.paiyipai.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class DepositRequestDTO {

    private Long id;
    private String paymentUrl;
    private String reason;

}
