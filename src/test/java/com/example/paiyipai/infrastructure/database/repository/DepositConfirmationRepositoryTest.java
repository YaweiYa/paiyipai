package com.example.paiyipai.infrastructure.database.repository;

import com.example.paiyipai.infrastructure.database.entity.DepositConfirmationEntity;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;

@DataJpaTest
public class DepositConfirmationRepositoryTest {

    @Autowired
    private DepositConfirmationRepository depositConfirmationRepository;

    @AfterEach
    void tearDown() {
        depositConfirmationRepository.deleteAll();
    }

    @Test
    void should_save_entity_correctly_when_save_entity() {
        var original = DepositConfirmationEntity.builder()
                .auctionId(10L)
                .pid("6")
                .result("paid")
                .createdAt(OffsetDateTime.now())
                .build();

        var result = depositConfirmationRepository.save(original);

        AssertionsForClassTypes.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(original);
    }
}
