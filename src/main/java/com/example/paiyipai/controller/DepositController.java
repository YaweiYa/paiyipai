package com.example.paiyipai.controller;

import com.example.paiyipai.dto.DepositRequestDTO;
import com.example.paiyipai.service.DepositService;
import com.example.paiyipai.service.exception.DepositRequestFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auctions/{auctionId}/deposits")
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    @ResponseBody
    public ResponseEntity<DepositRequestDTO> requestDeposit(@PathVariable Long auctionId) {
        try {
            var depositRequest = depositService.requestDeposit(auctionId);
            return ResponseEntity.ok(DepositRequestDTO.builder()
                    .id(depositRequest.getId())
                    .paymentUrl(depositRequest.getPaymentUrl())
                    .build());
        } catch (DepositRequestFailedException ex) {
            return ResponseEntity.internalServerError()
                    .body(DepositRequestDTO.builder()
                            .reason("Timeout to get payment URL")
                            .build());
        }
    }

    @PostMapping("/{depositId}/confirmation")
    public ResponseEntity<Void> confirmDeposit(@PathVariable Long auctionId, @PathVariable Long depositId, @RequestBody ConfirmDepositRequest confirmDepositRequest) {
        depositService.confirmDeposit(auctionId, depositId, confirmDepositRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{depositId}/reconfirmation")
    public ResponseEntity<Void> checkThenConfirmDeposit(@PathVariable Long auctionId, @PathVariable Long depositId) {
        depositService.checkThenConfirmDeposit(auctionId, depositId);
        return ResponseEntity.ok().build();
    }
}
