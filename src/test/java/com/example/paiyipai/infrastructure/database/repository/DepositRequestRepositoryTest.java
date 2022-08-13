package com.example.paiyipai.infrastructure.database.repository;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

@DataJpaTest
public class DepositRequestRepositoryTest {

    @Autowired
    private DepositRequestRepository depositRequestRepository;

    @AfterEach
    void tearDown() {
        depositRequestRepository.deleteAll();
    }

    @Test
    void should_save_entity_correctly_when_save_entity() {
        DepositRequestEntity original = DepositRequestEntity.builder()
                .auctionId(10L)
                .pid("5")
                .paymentUrl("XXXXXXXXXX")
                .createdAt(LocalDateTime.now())
                .build();

        DepositRequestEntity result = depositRequestRepository.save(original);

        AssertionsForClassTypes.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(original);
    }
}
