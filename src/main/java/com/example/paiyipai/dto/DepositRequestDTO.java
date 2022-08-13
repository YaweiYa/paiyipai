package com.example.paiyipai.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class DepositRequestDTO {

    private DepositRequestStatus status;

    private String reason;

    private String paymentUrl;

}
