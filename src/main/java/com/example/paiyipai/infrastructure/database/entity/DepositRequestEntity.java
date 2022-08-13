package com.example.paiyipai.infrastructure.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.LocalDateTime;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Builder
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "DepositRequest")
public class DepositRequestEntity {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;

    private Long auctionId;

    private String pid;

    private String paymentUrl;

    private LocalDateTime createdAt;
}
