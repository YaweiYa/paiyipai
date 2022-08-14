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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.lang.String.format;
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
    private static final Long DEPOSIT_ID = 1L;
    public static final String PID = "30";
    public static final String PAYMENT_URL = "https://mock-payment-url";
    public static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    public static final String RESULT = "paid";

    private DepositService depositService;

    @Mock
    private RestClient restClient;

    @Mock
    private DepositRequestRepository depositRequestRepository;

    @Mock
    private DepositConfirmationRepository depositConfirmationRepository;

    @BeforeEach
    void setUp() {
        depositService = new DepositService(restClient,
                depositRequestRepository,
                depositConfirmationRepository,
                CLOCK,
                PAYMENT_SERVICE_ENDPOINT);
    }

    @Test
    void should_processDepositRequest_when_requestDeposit() {
        var depositRequest = buildDepositRequest();
        var savedDepositRequest = buildDepositRequest(DEPOSIT_ID);
        var paymentResponse = PaymentResponse.builder().pid(PID).paymentUrl(PAYMENT_URL).build();
        when(depositRequestRepository.save(depositRequest)).thenReturn(savedDepositRequest);
        when(restClient.post(PAYMENT_SERVICE_URL, PaymentResponse.class)).thenReturn(ResponseEntity.ok(paymentResponse));

        var result = depositService.requestDeposit(AUCTION_ID);

        assertThat(result.getPaymentUrl()).isEqualTo(PAYMENT_URL);
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

    @Test
    void should_saveDepositConfirmation_when_confirmDeposit() {
        var confirmDepositRequest = ConfirmDepositRequest.builder().pid(PID).result(RESULT).build();
        var depositConfirmation = buildDepositConfirmation();

        when(depositConfirmationRepository.save(depositConfirmation)).thenReturn(any(DepositConfirmationEntity.class));

        depositService.confirmDeposit(AUCTION_ID, DEPOSIT_ID, confirmDepositRequest);

        verify(depositConfirmationRepository, times(1)).save(depositConfirmation);
    }

    @Test
    void should_checkThenConfirmDeposit_when_getDepositConfirmationSuccessfully() {
        var depositRequest = buildDepositRequest(1L);
        var paymentResponse = PaymentResultResponse.builder().pid(PID).result(RESULT).build();
        var depositConfirmation = buildDepositConfirmation();
        when(depositRequestRepository.findById(DEPOSIT_ID)).thenReturn(Optional.of(depositRequest));
        when(restClient.get(format("%s/payments/%s", PAYMENT_SERVICE_ENDPOINT, depositRequest.getPid()), PaymentResultResponse.class))
                .thenReturn(ResponseEntity.ok(paymentResponse));
        when(depositConfirmationRepository.save(depositConfirmation)).thenReturn(depositConfirmation);

        depositService.checkThenConfirmDeposit(AUCTION_ID, DEPOSIT_ID);

        verify(depositRequestRepository, times(1)).findById(DEPOSIT_ID);
        verify(restClient, times(1)).get(format("%s/payments/%s", PAYMENT_SERVICE_ENDPOINT, depositRequest.getPid()), PaymentResultResponse.class);
        verify(depositConfirmationRepository, times(1)).save(depositConfirmation);
    }

    private DepositRequestEntity buildDepositRequest() {
        return DepositRequestEntity.builder()
                .auctionId(AUCTION_ID)
                .pid(PID)
                .paymentUrl(PAYMENT_URL)
                .createdAt(OffsetDateTime.now(CLOCK))
                .build();
    }

    private DepositRequestEntity buildDepositRequest(Long id) {
        return DepositRequestEntity.builder()
                .id(id)
                .auctionId(AUCTION_ID)
                .pid(PID)
                .paymentUrl(PAYMENT_URL)
                .createdAt(OffsetDateTime.now(CLOCK))
                .build();
    }

    private DepositConfirmationEntity buildDepositConfirmation() {
        return DepositConfirmationEntity.builder()
                .auctionId(AUCTION_ID)
                .depositId(DEPOSIT_ID)
                .pid(PID)
                .result(RESULT)
                .createdAt(OffsetDateTime.now(CLOCK))
                .build();
    }

}
