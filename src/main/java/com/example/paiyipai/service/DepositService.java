package com.example.paiyipai.service;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import com.example.paiyipai.infrastructure.restful.RestClient;
import com.example.paiyipai.infrastructure.restful.model.PaymentResponse;
import com.example.paiyipai.infrastructure.database.repository.DepositRequestRepository;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class DepositService {

    private String paymentServiceEndpoint;

    private final RestClient restClient;

    private final DepositRequestRepository depositRequestRepository;

    public DepositService(RestClient restClient,
                          DepositRequestRepository depositRequestRepository,
                          @Value("${http.url.payment}") String paymentServiceEndpoint) {
        this.restClient = restClient;
        this.depositRequestRepository = depositRequestRepository;
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
                    .build();
            depositRequestRepository.save(depositRequest);

            return depositRequest.getPaymentUrl();
        } catch (RestClientException ex) {
            throw new DepositRequestFailedException(auctionId);
        }
    }
}
