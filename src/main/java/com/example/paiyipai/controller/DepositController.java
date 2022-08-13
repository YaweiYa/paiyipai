package com.example.paiyipai.controller;

import com.example.paiyipai.dto.DepositRequestDTO;
import com.example.paiyipai.service.DepositService;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static com.example.paiyipai.dto.DepositRequestStatus.FAILED;
import static com.example.paiyipai.dto.DepositRequestStatus.SUCCESS;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auctions/{id}/deposits")
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    @ResponseBody
    public ResponseEntity<DepositRequestDTO> requestDeposit(@PathVariable Long id) {
        try {
            var paymentUrl = depositService.requestDeposit(id);
            return ResponseEntity.ok(DepositRequestDTO.builder()
                    .status(SUCCESS)
                    .paymentUrl(paymentUrl)
                    .build());
        } catch (DepositRequestFailedException ex) {
            return ResponseEntity.internalServerError()
                    .body(DepositRequestDTO.builder()
                            .status(FAILED)
                            .reason("Timeout to get payment URL")
                            .build());
        }
    }
}
