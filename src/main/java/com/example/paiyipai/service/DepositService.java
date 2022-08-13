package com.example.paiyipai.service;

import com.example.paiyipai.controller.ConfirmDepositRequest;
import com.example.paiyipai.infrastructure.database.entity.DepositConfirmationEntity;
import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import com.example.paiyipai.infrastructure.database.repository.DepositConfirmationRepository;
import com.example.paiyipai.infrastructure.database.repository.DepositRequestRepository;
import com.example.paiyipai.infrastructure.restful.RestClient;
import com.example.paiyipai.infrastructure.restful.model.PaymentResponse;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class DepositService {

    private final String paymentServiceEndpoint;

    private final RestClient restClient;

    private final DepositRequestRepository depositRequestRepository;

    private final DepositConfirmationRepository depositConfirmationRepository;

    private final Clock clock;

    public DepositService(RestClient restClient,
                          DepositRequestRepository depositRequestRepository,
                          DepositConfirmationRepository depositConfirmationRepository,
                          Clock clock,
                          @Value("${http.url.payment}") String paymentServiceEndpoint) {
        this.restClient = restClient;
        this.depositRequestRepository = depositRequestRepository;
        this.depositConfirmationRepository = depositConfirmationRepository;
        this.clock = clock;
        this.paymentServiceEndpoint = paymentServiceEndpoint;
    }

    public String requestDeposit(Long auctionId) {
        try {
            var response = restClient.post(paymentServiceEndpoint + "/payments", PaymentResponse.class);

            if (response.getStatusCode().isError()) {
                throw new DepositRequestFailedException(auctionId);
            }

            var payment = response.getBody();
            var depositRequest = DepositRequestEntity.builder()
                    .auctionId(auctionId)
                    .pid(payment.getPid())
                    .paymentUrl(payment.getPaymentUrl())
                    .createdAt(OffsetDateTime.now(clock))
                    .build();
            depositRequestRepository.save(depositRequest);

            return depositRequest.getPaymentUrl();
        } catch (RestClientException ex) {
            throw new DepositRequestFailedException(auctionId);
        }
    }

    public void confirmDeposit(Long auctionId, ConfirmDepositRequest confirmDepositRequest) {
        var depositConfirmation = DepositConfirmationEntity.builder()
                .auctionId(auctionId)
                .pid(confirmDepositRequest.getPid())
                .result(confirmDepositRequest.getResult())
                .createdAt(OffsetDateTime.now(clock))
                .build();
        depositConfirmationRepository.save(depositConfirmation);
    }
}
