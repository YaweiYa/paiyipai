package com.example.paiyipai.infrastructure.database.repository;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositRequestRepository extends JpaRepository<DepositRequestEntity, Long> {
}
