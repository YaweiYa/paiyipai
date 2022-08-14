package com.example.paiyipai.service.model;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class DepositRequest {

    private Long id;

    private String paymentUrl;

    public static DepositRequest from(DepositRequestEntity depositRequestEntity) {
        return DepositRequest.builder()
                .id(depositRequestEntity.getId())
                .paymentUrl(depositRequestEntity.getPaymentUrl())
                .build();
    }
}
