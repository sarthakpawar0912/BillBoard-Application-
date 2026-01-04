package com.billboarding.Repository.Admin.Wallet;

import com.billboarding.ENUM.TxType;
import com.billboarding.Entity.ADMIN.wallet.AdminWalletTransaction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminWalletTransactionRepository extends JpaRepository<AdminWalletTransaction, Long> {

    // Order by id DESC for reliable sequential ordering (newest first)
    List<AdminWalletTransaction> findAllByOrderByIdDesc();

    List<AdminWalletTransaction> findByTypeOrderByIdDesc(TxType type);

    List<AdminWalletTransaction> findByTimeBetweenOrderByIdDesc(LocalDateTime start, LocalDateTime end);

    List<AdminWalletTransaction> findByBookingIdOrderByIdDesc(Long bookingId);

    List<AdminWalletTransaction> findByOwnerIdOrderByIdDesc(Long ownerId);

    @Query("SELECT SUM(t.amount) FROM AdminWalletTransaction t WHERE t.type = :type")
    Double sumByType(@Param("type") TxType type);

    @Query("SELECT SUM(t.amount) FROM AdminWalletTransaction t WHERE t.type = :type AND t.time >= :start")
    Double sumByTypeAfterDate(@Param("type") TxType type, @Param("start") LocalDateTime start);

    @Query("SELECT COUNT(t) FROM AdminWalletTransaction t WHERE t.type = :type")
    Long countByType(@Param("type") TxType type);

    @Modifying
    @Transactional
    @Query("UPDATE AdminWalletTransaction t SET t.bookingId = null WHERE t.bookingId IN :bookingIds")
    void nullifyBookingIdIn(@org.springframework.data.repository.query.Param("bookingIds") java.util.List<Long> bookingIds);
}
