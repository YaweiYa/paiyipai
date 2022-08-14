package com.example.paiyipai.infrastructure.database.repository;

import com.example.paiyipai.infrastructure.database.entity.DepositRequestEntity;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.Optional;

@DataJpaTest
public class DepositRequestRepositoryTest {

    @Autowired
    private DepositRequestRepository depositRequestRepository;

    @AfterEach
    void tearDown() {
        depositRequestRepository.deleteAll();
    }

    @Test
    void should_saveEntityCorrectly_when_saveEntity() {
        var original = DepositRequestEntity.builder()
                .auctionId(10L)
                .pid("5")
                .paymentUrl("XXXXXXXXXX")
                .createdAt(OffsetDateTime.now())
                .build();

        var result = depositRequestRepository.save(original);

        AssertionsForClassTypes.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(original);
    }

    @Test
    void should_findEntityByIdCorrectly_when_findById() {
        var original = DepositRequestEntity.builder()
                .auctionId(10L)
                .pid("5")
                .paymentUrl("XXXXXXXXXX")
                .createdAt(OffsetDateTime.now())
                .build();
        var entity = depositRequestRepository.save(original);

        var result = depositRequestRepository.findById(entity.getId());

        AssertionsForClassTypes.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(entity));
    }
}
