package io.github.nadimnesar.producer.repository;

import io.github.nadimnesar.producer.entity.SmsMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for managing SMS message records in PostgreSQL.
 */
@Repository
public interface SmsRepository extends JpaRepository<SmsMessageEntity, String> {

    List<SmsMessageEntity> findByRecipientPhoneNumber(String recipientPhoneNumber);
}
