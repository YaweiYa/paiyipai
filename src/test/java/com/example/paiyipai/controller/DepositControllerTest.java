package com.example.paiyipai.controller;

import com.example.paiyipai.infrastructure.database.repository.DepositRequestRepository;
import com.example.paiyipai.service.DepositService;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class DepositControllerTest {
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
    void should_get200AndPaymentUrl_when_callRequestDepositEndpoint() {
        var captor = ArgumentCaptor.forClass(Long.class);
        when(depositService.requestDeposit(captor.capture())).thenReturn("mock-payment-url");

        mockMvc.perform(post("/auctions/1/deposits"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"paymentUrl\":\"mock-payment-url\"}"));

        Long auctionId = captor.getValue();

        verify(depositService).requestDeposit(eq(auctionId));
    }

    @SneakyThrows
    @Test
    void should_get500AndReason_when_callRequestDepositEndpointFailed() {
        var captor = ArgumentCaptor.forClass(Long.class);
        when(depositService.requestDeposit(captor.capture())).thenThrow(new DepositRequestFailedException(1L));

        mockMvc.perform(post("/auctions/1/deposits"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"reason\":\"Timeout to get payment URL\"}"));

        Long auctionId = captor.getValue();

        verify(depositService).requestDeposit(eq(auctionId));
    }
}
