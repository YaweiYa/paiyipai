package com.example.paiyipai.service;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import com.example.paiyipai.infrastructure.restful.RestClient;
import com.example.paiyipai.infrastructure.restful.model.PaymentResponse;
import com.example.paiyipai.infrastructure.database.repository.DepositRequestRepository;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    public static final String PAYMENT_SERVICE_ENDPOINT = "https://3rd-party-payment";
    public static final String PAYMENT_SERVICE_URL = PAYMENT_SERVICE_ENDPOINT + "/payments";
    public static final long AUCTION_ID = 6L;
    public static final String PID = "30";
    public static final String PAYMENT_URL = "https://mock-payment-url";
    private DepositService depositService;

    @Mock
    private RestClient restClient;

    @Mock
    private DepositRequestRepository depositRequestRepository;

    @BeforeEach
    void setUp() {
        depositService = new DepositService(restClient, depositRequestRepository, PAYMENT_SERVICE_ENDPOINT);
    }

    @Test
    void should_processDepositRequest_when_requestDeposit() {
        var depositRequest = DepositRequestEntity.builder()
                .auctionId(AUCTION_ID)
                .pid(PID)
                .paymentUrl(PAYMENT_URL)
                .build();
        var savedDepositRequest = DepositRequestEntity.builder()
                .id(1L)
                .auctionId(AUCTION_ID)
                .pid(PID)
                .paymentUrl(PAYMENT_URL)
                .build();
        var paymentResponse = PaymentResponse.builder().pid(PID).paymentUrl(PAYMENT_URL).build();
        when(depositRequestRepository.save(depositRequest)).thenReturn(savedDepositRequest);
        when(restClient.post(PAYMENT_SERVICE_URL, PaymentResponse.class)).thenReturn(ResponseEntity.ok(paymentResponse));

        var result = depositService.requestDeposit(AUCTION_ID);

        assertThat(result).isEqualTo(PAYMENT_URL);
        verify(depositRequestRepository, times(1)).save(depositRequest);
        verify(restClient, times(1)).post(PAYMENT_SERVICE_URL, PaymentResponse.class);
    }

    @Test
    void should_throwDepositRequestFailedException_when_failedToGetPaymentUrl() {
        when(restClient.post(PAYMENT_SERVICE_URL, PaymentResponse.class)).thenReturn(ResponseEntity.badRequest().build());

        assertThatThrownBy(() -> depositService.requestDeposit(AUCTION_ID)).isInstanceOf(DepositRequestFailedException.class);
        verify(restClient, times(1)).post(PAYMENT_SERVICE_URL, PaymentResponse.class);
        verify(depositRequestRepository, times(0)).save(any(DepositRequestEntity.class));
    }

    @Test
    void should_throwDepositRequestFailedException_when_timeoutToGetPaymentUrl() {
        doThrow(new RestClientException("timeout")).when(restClient).post(any(), eq(PaymentResponse.class));

        assertThatThrownBy(() -> depositService.requestDeposit(AUCTION_ID)).isInstanceOf(DepositRequestFailedException.class);
        verify(restClient, times(1)).post(PAYMENT_SERVICE_URL, PaymentResponse.class);
        verify(depositRequestRepository, times(0)).save(any(DepositRequestEntity.class));
    }
}
