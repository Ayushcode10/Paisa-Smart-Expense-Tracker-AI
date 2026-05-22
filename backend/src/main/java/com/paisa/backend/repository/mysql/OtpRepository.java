package com.paisa.backend.repository.mysql;

import com.paisa.backend.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord,Long> {

    //get the latest verified OTP for this phone
    Optional<OtpRecord> findTopByPhoneAndVerifiedFalseOrderByCreatedAtDesc(String phone);

    //clean up old OTPs for a phone before sending new one
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpRecord o WHERE o.phone = :phone")
    void deleteAllByPhone(String phone);
}
