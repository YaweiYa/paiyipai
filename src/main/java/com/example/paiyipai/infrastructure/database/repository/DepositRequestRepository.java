package com.example.paiyipai.infrastructure.database.repository;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositRequestRepository extends JpaRepository<DepositRequestEntity, Long> {

    Optional<DepositRequestEntity> findByAuctionId(Long auctionId);
}
