package com.example.paiyipai.infrastructure.database.repository;

import com.example.paiyipai.infrastructure.database.entity.DepositConfirmationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositConfirmationRepository extends JpaRepository<DepositConfirmationEntity, Long> {
}
