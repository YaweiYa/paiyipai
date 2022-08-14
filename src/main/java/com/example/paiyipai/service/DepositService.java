package com.example.paiyipai.service;

import com.example.paiyipai.controller.ConfirmDepositRequest;
import com.example.paiyipai.infrastructure.database.entity.DepositConfirmationEntity;
import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import com.example.paiyipai.infrastructure.database.repository.DepositConfirmationRepository;
import com.example.paiyipai.infrastructure.database.repository.DepositRequestRepository;
import com.example.paiyipai.infrastructure.restful.RestClient;
import com.example.paiyipai.infrastructure.restful.model.PaymentResponse;
import com.example.paiyipai.infrastructure.restful.model.PaymentResultResponse;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import com.example.paiyipai.service.model.DepositRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.OffsetDateTime;

import static java.lang.String.format;

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

    public DepositRequest requestDeposit(Long auctionId) {
        try {
            var response = restClient.post(paymentServiceEndpoint + "/payments", PaymentResponse.class);

            if (response.getStatusCode().isError()) {
                throw new DepositRequestFailedException(auctionId);
            }

            var paymentResponse = response.getBody();
            var depositRequestEntity = DepositRequestEntity.builder()
                    .auctionId(auctionId)
                    .pid(paymentResponse.getPid())
                    .paymentUrl(paymentResponse.getPaymentUrl())
                    .createdAt(OffsetDateTime.now(clock))
                    .build();
            var savedDepositRequestEntity =  depositRequestRepository.save(depositRequestEntity);

            return DepositRequest.from(savedDepositRequestEntity);
        } catch (RestClientException ex) {
            throw new DepositRequestFailedException(auctionId);
        }
    }

    public void confirmDeposit(Long auctionId, Long depositId, ConfirmDepositRequest confirmDepositRequest) {
        var depositConfirmation = DepositConfirmationEntity.builder()
                .auctionId(auctionId)
                .depositId(depositId)
                .pid(confirmDepositRequest.getPid())
                .result(confirmDepositRequest.getResult())
                .createdAt(OffsetDateTime.now(clock))
                .build();
        depositConfirmationRepository.save(depositConfirmation);
    }

    public void checkThenConfirmDeposit(Long auctionId, Long depositId) {
        var depositRequest = depositRequestRepository.findById(depositId).get();
        var paymentResult = restClient.get(
                        format("%s/payments/%s", paymentServiceEndpoint, depositRequest.getPid()),
                        PaymentResultResponse.class)
                .getBody();

        var depositConfirmation = DepositConfirmationEntity.builder()
                .auctionId(auctionId)
                .depositId(depositRequest.getId())
                .pid(paymentResult.getPid())
                .result(paymentResult.getResult())
                .createdAt(OffsetDateTime.now(clock))
                .build();

        depositConfirmationRepository.save(depositConfirmation);
    }
}
