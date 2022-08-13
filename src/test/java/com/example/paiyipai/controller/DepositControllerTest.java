package com.example.paiyipai.controller;

import com.example.paiyipai.infrastructure.database.repository.DepositRequestRepository;
import com.example.paiyipai.service.DepositService;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class DepositControllerTest {

    private static final Long AUCTION_ID = 6L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepositRequestRepository depositRequestRepository;

    @MockBean
    private DepositService depositService;

    @AfterEach
    void tearDown() {
        depositRequestRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    void should_return200AndPaymentUrl_when_callRequestDepositEndpoint() {
        var captor = ArgumentCaptor.forClass(Long.class);
        when(depositService.requestDeposit(captor.capture())).thenReturn("mock-payment-url");

        mockMvc.perform(post(format("/auctions/%s/deposit", AUCTION_ID)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"paymentUrl\":\"mock-payment-url\"}"));

        Long auctionId = captor.getValue();

        verify(depositService).requestDeposit(eq(auctionId));
    }

    @SneakyThrows
    @Test
    void should_return500AndReason_when_callRequestDepositEndpointFailed() {
        var captor = ArgumentCaptor.forClass(Long.class);
        when(depositService.requestDeposit(captor.capture())).thenThrow(new DepositRequestFailedException(1L));

        mockMvc.perform(post(format("/auctions/%s/deposit", AUCTION_ID)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"reason\":\"Timeout to get payment URL\"}"));

        Long auctionId = captor.getValue();

        verify(depositService).requestDeposit(eq(auctionId));
    }

    @SneakyThrows
    @Test
    void should_return200_when_callConfirmDepositEndpoint() {
        var confirmDepositRequest = ConfirmDepositRequest.builder().pid("30").result("paid").build();
        doNothing().when(depositService).confirmDeposit(AUCTION_ID, confirmDepositRequest);

        mockMvc.perform(post(format("/auctions/%s/deposit/confirmation", AUCTION_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(confirmDepositRequest)))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void should_return200_when_callReconfirmDepositEndpoint() {
        doNothing().when(depositService).checkThenConfirmDeposit(AUCTION_ID);

        mockMvc.perform(post(format("/auctions/%s/deposit/reconfirmation", AUCTION_ID)))
                .andExpect(status().isOk());
    }
}
