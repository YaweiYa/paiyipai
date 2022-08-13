package com.example.paiyipai.service.exception;

public class DepositRequestFailedException extends RuntimeException {

    public DepositRequestFailedException(Long auctionId) {
        super(String.format("Failed to get payment URL for auctionId<%s> ", auctionId));
    }
}
